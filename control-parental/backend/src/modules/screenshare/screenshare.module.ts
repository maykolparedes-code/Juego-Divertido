import { Module } from '@nestjs/common';
import { ScreenShareGateway } from './screenshare.gateway';

@Module({
  providers: [ScreenShareGateway],
})
export class ScreenShareModule {}
