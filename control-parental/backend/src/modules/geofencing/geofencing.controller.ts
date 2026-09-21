import { Body, Controller, Get, Param, Post } from '@nestjs/common';
import { IsIn, IsNumber, IsString, Max, Min } from 'class-validator';
import { GeofencingService } from './geofencing.service';

class CreateGeofenceDto {
  @IsString() familyId!: string;
  @IsString() name!: string;
  @IsNumber() @Min(-90) @Max(90) lat!: number;
  @IsNumber() @Min(-180) @Max(180) lng!: number;
  @IsNumber() @Min(10) radiusMeters!: number;
}

class GeofenceEventDto {
  @IsString() geofenceId!: string;
  @IsString() deviceId!: string;
  @IsString() familyId!: string;
  @IsIn(['ENTER', 'EXIT']) type!: 'ENTER' | 'EXIT';
}

@Controller('geofences')
export class GeofencingController {
  constructor(private readonly geofencing: GeofencingService) {}

  @Post()
  create(@Body() dto: CreateGeofenceDto) {
    return this.geofencing.createGeofence(dto);
  }

  @Get('family/:familyId')
  list(@Param('familyId') familyId: string) {
    return this.geofencing.listGeofences(familyId);
  }

  @Post('events')
  recordEvent(@Body() dto: GeofenceEventDto) {
    return this.geofencing.recordEvent(dto);
  }
}
