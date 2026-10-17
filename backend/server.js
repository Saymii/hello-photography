import express from 'express';
import {rateLimit} from 'express-rate-limit';
import {initializeApp, applicationDefault} from 'firebase-admin/app';
import {getAuth} from 'firebase-admin/auth';
import {getFirestore, FieldValue} from 'firebase-admin/firestore';
import {google} from 'googleapis';
import {createHash} from 'node:crypto';
import {deriveEntitlement} from './entitlement.js';

const packageName = process.env.ANDROID_PACKAGE;
const products = [process.env.PLAY_MONTHLY_PRODUCT, process.env.PLAY_ANNUAL_PRODUCT];
if(!packageName || products.some(p => !p)) throw new Error('Set ANDROID_PACKAGE, PLAY_MONTHLY_PRODUCT and PLAY_ANNUAL_PRODUCT');
initializeApp({credential:applicationDefault()});
const db=getFirestore();
const publisher=google.androidpublisher({version:'v3',auth:new google.auth.GoogleAuth({scopes:['https://www.googleapis.com/auth/androidpublisher']})});
const app=express();
app.disable('x-powered-by');
app.use(express.json({limit:'16kb'}));
app.use(rateLimit({windowMs:60_000,limit:30}));
app.get('/health',(_,res)=>res.json({status:'ok',billing:'requires authenticated verification'}));
app.use(async(req,res,next)=>{
 try {
  const match=/^Bearer (.+)$/.exec(req.headers.authorization || '');
  if(!match) return res.status(401).json({error:'Authentication required'});
  req.user=await getAuth().verifyIdToken(match[1],true);
  next();
 } catch {res.status(401).json({error:'Invalid authentication'});}
});
app.post('/v1/purchases/verify',async(req,res)=>{
 try {
  const {productId,purchaseToken}=req.body;
  if(!products.includes(productId) || typeof purchaseToken!=='string' || purchaseToken.length<10 || purchaseToken.length>4096) return res.status(400).json({error:'Invalid purchase request'});
  const tokenHash=createHash('sha256').update(purchaseToken).digest('hex');
  const response=await publisher.purchases.subscriptionsv2.get({packageName,token:purchaseToken});
  const sub=response.data;
  if(!sub.lineItems?.some(line=>line.productId===productId)) return res.status(400).json({error:'Product mismatch'});
  // Purchase launch must set obfuscatedAccountId to this UID hash.
  const expected=createHash('sha256').update(req.user.uid).digest('hex');
  if(sub.externalAccountIdentifiers?.obfuscatedExternalAccountId!==expected) return res.status(403).json({error:'Purchase account mismatch'});
  const entitlement=deriveEntitlement(sub,products);
  const owner=db.collection('purchaseOwners').doc(tokenHash);
  await db.runTransaction(async tx=>{
   const prior=await tx.get(owner);
   if(prior.exists && prior.data().uid!==req.user.uid) throw new Error('Ownership conflict');
   tx.set(owner,{uid:req.user.uid,updatedAt:FieldValue.serverTimestamp()});
  });
  if(entitlement.premium && sub.acknowledgementState==='ACKNOWLEDGEMENT_STATE_PENDING') {
   await publisher.purchases.subscriptions.acknowledge({packageName,subscriptionId:productId,token:purchaseToken,requestBody:{}});
  }
  await db.collection('entitlements').doc(req.user.uid).set({...entitlement,verifiedAt:FieldValue.serverTimestamp()});
  res.json(entitlement);
 } catch {
  // Deliberately omit tokens, identity and upstream error details from logs and responses.
  res.status(503).json({error:'Purchase could not be verified. Retry later.'});
 }
});
app.get('/v1/entitlement',async(req,res)=>{
 try {
  const doc=await db.collection('entitlements').doc(req.user.uid).get();
  const data=doc.data();
  res.json({premium:!!data?.premium && data.expiresAtEpochMillis>Date.now(),expiresAtEpochMillis:data?.expiresAtEpochMillis || 0});
 } catch {res.status(503).json({error:'Entitlement unavailable'});}
});
app.use((err,req,res,next)=>{res.status(400).json({error:'Invalid request'});});
app.listen(Number(process.env.PORT || 8080),'127.0.0.1');
