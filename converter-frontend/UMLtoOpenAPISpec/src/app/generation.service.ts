import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GenerationService {
  private baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  parseDiagramElements(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.baseUrl}/parse-elements`, formData);
  }

  generateSpec(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.baseUrl}/generate`, formData, {
      responseType: 'text'
    });
  }

  applyMappings(mappings: any[]): Observable<any> {
    const payload = { mappings: mappings };
    return this.http.post(`${this.baseUrl}/apply-mappings`, payload);
  }
}
