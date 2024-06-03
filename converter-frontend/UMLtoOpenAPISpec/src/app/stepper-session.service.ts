import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StepperSessionService {
  private baseUrl = '/api/sessions';

  constructor(private http: HttpClient) {}

  saveSession(session: any): Observable<any> {
    return this.http.post(this.baseUrl, session);
  }

  getAllSessions(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }

  getSession(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }
}
