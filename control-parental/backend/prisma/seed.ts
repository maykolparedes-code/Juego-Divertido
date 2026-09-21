import { PrismaClient } from '@prisma/client';
import { randomBytes } from 'crypto';
import * as bcrypt from 'bcrypt';

/**
 * Datos de ejemplo para probar la API localmente: una familia con un
 * padre y un hijo, cada uno con su dispositivo. Los IDs generados se
 * imprimen al final para usarlos en las pruebas manuales (curl, Postman).
 */
const prisma = new PrismaClient();

async function main() {
  const family = await prisma.family.create({
    data: {
      name: 'Familia Demo',
      inviteCode: randomBytes(4).toString('hex').toUpperCase(),
    },
  });

  const parent = await prisma.user.create({
    data: {
      familyId: family.id,
      role: 'PARENT',
      name: 'Mamá/Papá Demo',
      email: 'padre@demo.local',
      passwordHash: await bcrypt.hash('demo1234', 12),
    },
  });

  const child = await prisma.user.create({
    data: {
      familyId: family.id,
      role: 'CHILD',
      name: 'Hijo/a Demo',
      email: 'hijo@demo.local',
      passwordHash: await bcrypt.hash('demo1234', 12),
    },
  });

  const parentDevice = await prisma.device.create({
    data: {
      familyId: family.id,
      ownerUserId: parent.id,
      platform: 'ANDROID',
      model: 'Pixel 8 (padre)',
      pushToken: 'demo-push-token-padre',
    },
  });

  const childDevice = await prisma.device.create({
    data: {
      familyId: family.id,
      ownerUserId: child.id,
      platform: 'ANDROID',
      model: 'Pixel 6a (hijo)',
    },
  });

  console.log('--- Datos de prueba creados ---');
  console.log({
    familyId: family.id,
    inviteCode: family.inviteCode,
    parentDeviceId: parentDevice.id,
    childDeviceId: childDevice.id,
  });
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
