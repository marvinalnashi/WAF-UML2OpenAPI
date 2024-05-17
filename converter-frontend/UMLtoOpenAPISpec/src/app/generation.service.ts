import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
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
    return this.http.post<any>(`${this.baseUrl}/parse-elements`, formData);
  }

  // generateSpec(file: FormData): Observable<string> {
  //   const formData = new FormData();
  //   formData.append('file', file);
  //   return this.http.post<string>(`${this.baseUrl}/generate`, formData, { responseType: 'text' as 'json' });
  // }

  generateSpec(formData: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/generate`, formData);
  }

  applyMappings(mappings: any[]): Observable<any> {
    return this.http.post(`${this.baseUrl}/apply-mappings`, mappings, {
      context: undefined,
      observe: "body",
      params: undefined,
      reportProgress: false,
      responseType: "arraybuffer",
      transferCache: undefined,
      withCredentials: false,
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    });
  }

  renameElement(type: string, oldName: string, newName: string): Observable<string> {
    return this.http.post<string>(`${this.baseUrl}/rename-element`, { type, oldName, newName }, { responseType: 'text' as 'json' });
  }

  deleteElement(type: string, name: string): Observable<string> {
    return this.http.post<string>(`${this.baseUrl}/delete-element`, { type, name }, { responseType: 'text' as 'json' });
  }
}
