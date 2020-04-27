import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {ApiClientBase} from './api-client-base.service';
import {CreateReservationDto} from '../data/client/model/createReservationDto';
import {SlotsDto} from '../data/client/model/slotsDto';
import {SlotConfigDto} from '../data/client/model/slotConfigDto';

const API = '/api/reservation';
const SLOT_DAYS_DEFAULT = 7;

@Injectable({providedIn: 'root'})
export class ReservationClient {

  constructor(private http: HttpClient, private base: ApiClientBase) {
  }

  createReservation(shopId: string, request: CreateReservationDto): Promise<void> {
    return this.base.promisify(
      this.http.post(`${API}/${shopId}`, request),
      '[ReservationClient] failed to create reservation'
    );
  }

  cancelReservation(token: string): Promise<void> {
    return this.base.promisify(
      this.http.delete(API, {
        params: new HttpParams().set('token', token)
      }),
      '[ReservationClient] failed to cancel reservation'
    );
  }

  getSlotsForShop(shopId: string, days = SLOT_DAYS_DEFAULT): Promise<SlotsDto> {
    return this.base.promisify(
      this.http.get(`${API}/${shopId}/slot`, {
        params: new HttpParams().set('days', `${days}`)
      }),
      '[ReservationClient] failed to get slots for shop'
    );
  }

  previewSlots(request: SlotConfigDto): Promise<SlotsDto> {
    return this.base.promisify(
      this.http.post(`${API}/preview`, request),
      '[ReservationClient] failed to get slot previews'
    );
  }

}
