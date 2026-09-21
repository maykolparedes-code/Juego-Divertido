import { Injectable, Logger } from '@nestjs/common';
import * as admin from 'firebase-admin';

/**
 * Envío de notificaciones push (FCM) a los dispositivos de los padres
 * cuando ocurre una alerta (SOS, geocerca, límite de tiempo alcanzado).
 * Usa prioridad alta para que llegue aunque la app esté cerrada.
 */
@Injectable()
export class PushService {
  private readonly logger = new Logger(PushService.name);

  async sendAlert(
    pushToken: string,
    title: string,
    body: string,
    data: Record<string, string>,
  ): Promise<void> {
    try {
      await admin.messaging().send({
        token: pushToken,
        notification: { title, body },
        data,
        android: { priority: 'high' },
        apns: { headers: { 'apns-priority': '10' } },
      });
    } catch (error) {
      this.logger.error(`No se pudo enviar push a ${pushToken}`, error as Error);
    }
  }
}
