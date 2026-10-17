/** Privileged local CLI. Use a least-privilege ADC identity; never ship credentials to Android. */
import {readFile} from 'node:fs/promises';
import {initializeApp,applicationDefault} from 'firebase-admin/app';
import {getFirestore} from 'firebase-admin/firestore';
const file=process.argv[2];
if(!file) throw new Error('Usage: npm run publish:catalog -- /path/to/reviewed-catalog.json');
const poses=JSON.parse(await readFile(file,'utf8'));
if(!Array.isArray(poses) || poses.length>400) throw new Error('Expected at most 400 poses per batch');
const ids=new Set();
for(const pose of poses) {
 if(!Number.isInteger(pose.id) || ids.has(pose.id) || typeof pose.name!=='string' || typeof pose.steps!=='string' || typeof pose.premium!=='boolean') throw new Error('Invalid catalog schema');
 if(pose.imageUrl && (!pose.license || !pose.license.source || !pose.license.permission)) throw new Error('Reference image requires recorded license evidence');
 ids.add(pose.id);
}
initializeApp({credential:applicationDefault()});
const db=getFirestore(),batch=db.batch();
for(const pose of poses) batch.set(db.collection('catalog').doc(String(pose.id)),{...pose,published:false});
await batch.commit();
console.log(`Uploaded ${poses.length} unpublished catalog drafts. Review before publication.`);
