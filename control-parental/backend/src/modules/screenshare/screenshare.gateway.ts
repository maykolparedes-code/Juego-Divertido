import {
  WebSocketGateway,
  WebSocketServer,
  SubscribeMessage,
  ConnectedSocket,
  MessageBody,
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';

/**
 * Canal en tiempo real para compartir pantalla BAJO PEDIDO.
 *
 * Flujo, siempre con consentimiento explícito en cada paso:
 *  1. El padre pide ver la pantalla ('screen-share:request'). El servidor
 *     solo reenvía el pedido a la familia — no autoriza nada por su cuenta.
 *  2. El menor ve un aviso claro en su propia app y decide aceptar o
 *     rechazar ('screen-share:response'). El servidor reenvía la
 *     respuesta al padre.
 *  3. Si acepta, Android además exige su propio diálogo de sistema
 *     (MediaProjection) antes de poder capturar un solo cuadro — eso no
 *     lo controla ni lo puede saltar este backend ni la app.
 *  4. Mientras dura, el menor ve la notificación de "grabando pantalla"
 *     obligatoria del sistema operativo y un botón "Detener" en su app.
 *  5. Los cuadros (capturas periódicas, no video continuo — ver
 *     ARCHITECTURE.md) se reenvían tal cual, sin guardarse en el backend.
 *
 * El servidor nunca decide iniciar una sesión de captura por sí solo: es
 * un simple relé entre dos consentimientos explícitos.
 */
@WebSocketGateway({ namespace: 'screen-share', cors: true })
export class ScreenShareGateway {
  @WebSocketServer()
  server!: Server;

  @SubscribeMessage('join-family')
  handleJoinFamily(
    @ConnectedSocket() client: Socket,
    @MessageBody() data: { familyId: string },
  ) {
    client.join(`family:${data.familyId}`);
  }

  @SubscribeMessage('screen-share:request')
  handleRequest(
    @ConnectedSocket() client: Socket,
    @MessageBody() data: { familyId: string; deviceId: string },
  ) {
    client.to(`family:${data.familyId}`).emit('screen-share:request', {
      deviceId: data.deviceId,
    });
  }

  @SubscribeMessage('screen-share:response')
  handleResponse(
    @ConnectedSocket() client: Socket,
    @MessageBody() data: { familyId: string; deviceId: string; accepted: boolean },
  ) {
    client.to(`family:${data.familyId}`).emit('screen-share:response', {
      deviceId: data.deviceId,
      accepted: data.accepted,
    });
  }

  @SubscribeMessage('screen-share:frame')
  handleFrame(
    @ConnectedSocket() client: Socket,
    @MessageBody() data: { familyId: string; deviceId: string; jpegBase64: string },
  ) {
    // No se persiste ni se registra el contenido del cuadro — solo se
    // reenvía en vivo a quien esté viendo esa familia en ese momento.
    client.to(`family:${data.familyId}`).emit('screen-share:frame', {
      deviceId: data.deviceId,
      jpegBase64: data.jpegBase64,
    });
  }

  @SubscribeMessage('screen-share:stop')
  handleStop(
    @ConnectedSocket() client: Socket,
    @MessageBody() data: { familyId: string; deviceId: string },
  ) {
    client.to(`family:${data.familyId}`).emit('screen-share:stop', {
      deviceId: data.deviceId,
    });
  }
}
