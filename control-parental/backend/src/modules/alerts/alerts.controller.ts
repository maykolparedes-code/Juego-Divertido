import { Body, Controller, Post } from '@nestjs/common';
import { IsIn, IsNumber, IsOptional, IsString } from 'class-validator';
import { AlertsService } from './alerts.service';

class CreateAlertDto {
  @IsString() familyId!: string;
  @IsString() deviceId!: string;
  @IsIn(['SOS', 'GEOFENCE_ENTER', 'GEOFENCE_EXIT', 'SCREEN_TIME_LIMIT_REACHED'])
  type!: 'SOS' | 'GEOFENCE_ENTER' | 'GEOFENCE_EXIT' | 'SCREEN_TIME_LIMIT_REACHED';
  @IsOptional() @IsNumber() lat?: number;
  @IsOptional() @IsNumber() lng?: number;
  @IsOptional() @IsString() message?: string;
}

@Controller('alerts')
export class AlertsController {
  constructor(private readonly alerts: AlertsService) {}

  /**
   * Endpoint del botón de pánico. Sin autenticación adicional más allá del
   * JWT del dispositivo del menor: debe ser lo más rápido posible en una
   * emergencia real.
   */
  @Post()
  create(@Body() dto: CreateAlertDto) {
    return this.alerts.create(dto);
  }
}
