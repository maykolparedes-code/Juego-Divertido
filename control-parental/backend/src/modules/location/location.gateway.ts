import {
  WebSocketGateway,
  WebSocketServer,
  SubscribeMessage,
  ConnectedSocket,
  MessageBody,
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';

/**
 * Canal en tiempo real para el mapa en vivo del padre. Cada familia tiene
 * su propia "room" de Socket.IO; solo dispositivos autenticados con un JWT
 * válido para esa familia pueden unirse (validación en el middleware de
 * conexión, omitida aquí por brevedad).
 */
@WebSocketGateway({ namespace: 'live', cors: true })
export class LocationGateway {
  @WebSocketServer()
  server!: Server;

  @SubscribeMessage('join-family')
  handleJoinFamily(
    @ConnectedSocket() client: Socket,
    @MessageBody() data: { familyId: string },
  ) {
    client.join(`family:${data.familyId}`);
  }

  broadcastToFamily(familyId: string, event: string, payload: unknown) {
    this.server.to(`family:${familyId}`).emit(event, payload);
  }
}
