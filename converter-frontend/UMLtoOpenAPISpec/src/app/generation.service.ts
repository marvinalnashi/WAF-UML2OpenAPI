import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class GenerationService {
  private baseUrl = 'http://localhost:8080';
  private generateUrl = `${this.baseUrl}/generate`;

  constructor(private http: HttpClient) { }

  generateSpec(file: File): Observable<string> {
    const formData: FormData = new FormData();
    formData.append('file', file, file.name);

    return this.http.post(this.generateUrl, formData, {
      responseType: 'text'
    });
  }
}
