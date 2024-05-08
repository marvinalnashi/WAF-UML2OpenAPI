import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class MockServerService {
  private baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  toggleMockServer(): Observable<any> {
    return this.http.get(`${this.baseUrl}/toggle-prism-mock`);
  }
}
