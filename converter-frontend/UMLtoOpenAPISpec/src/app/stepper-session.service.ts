import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StepperSessionService {
  private baseUrl = 'http://localhost:8080/api/sessions';

  constructor(private http: HttpClient) {}

  saveSession(umlDiagram: File, openApiSpec: string): Observable<any> {
    const formData: FormData = new FormData();
    formData.append('umlDiagram', umlDiagram);
    formData.append('openApiSpec', openApiSpec);

    return this.http.post(this.baseUrl, formData, {
      headers: new HttpHeaders({
        'Accept': 'application/json'
      })
    });
  }

  getAllSessions(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }

  getSession(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }
}
