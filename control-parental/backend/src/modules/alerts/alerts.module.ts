import { Module } from '@nestjs/common';
import { AlertsController } from './alerts.controller';
import { AlertsService } from './alerts.service';
import { PushService } from '../../common/push/push.service';
import { EncryptionService } from '../../common/encryption/encryption.service';

@Module({
  controllers: [AlertsController],
  providers: [AlertsService, PushService, EncryptionService],
  exports: [AlertsService],
})
export class AlertsModule {}
