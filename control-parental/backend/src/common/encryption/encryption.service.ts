import { Injectable } from '@nestjs/common';
import { createCipheriv, createDecipheriv, randomBytes } from 'crypto';

/**
 * Cifrado en reposo (AES-256-GCM) para columnas sensibles (lat/lng).
 * La clave real de producción debe venir de un KMS (AWS KMS, GCP KMS,
 * HashiCorp Vault), una por familia — aquí se deriva de una env var solo
 * para desarrollo local.
 */
@Injectable()
export class EncryptionService {
  private readonly key = Buffer.from(
    process.env.LOCATION_ENCRYPTION_KEY ?? '',
    'base64',
  );

  encrypt(plaintext: string): string {
    const iv = randomBytes(12);
    const cipher = createCipheriv('aes-256-gcm', this.key, iv);
    const encrypted = Buffer.concat([
      cipher.update(plaintext, 'utf8'),
      cipher.final(),
    ]);
    const authTag = cipher.getAuthTag();
    // iv (12) + authTag (16) + ciphertext, todo en base64
    return Buffer.concat([iv, authTag, encrypted]).toString('base64');
  }

  decrypt(payload: string): string {
    const raw = Buffer.from(payload, 'base64');
    const iv = raw.subarray(0, 12);
    const authTag = raw.subarray(12, 28);
    const ciphertext = raw.subarray(28);
    const decipher = createDecipheriv('aes-256-gcm', this.key, iv);
    decipher.setAuthTag(authTag);
    return Buffer.concat([
      decipher.update(ciphertext),
      decipher.final(),
    ]).toString('utf8');
  }
}
