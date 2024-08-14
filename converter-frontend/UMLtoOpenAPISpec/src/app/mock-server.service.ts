import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {catchError, Observable, throwError} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class MockServerService {
  private baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  toggleMockServer(): Observable<any> {
    return this.http.get(`${this.baseUrl}/toggle-prism-mock`);
  }

  testOpenApiSpecification(): Observable<any> {
    return this.http.get(`${this.baseUrl}/test-openapi`);
  }

  testApi(method: string, path: string, body?: any): Observable<any> {
    const url = `${this.baseUrl}${path}`;
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    switch (method) {
      case 'POST':
        return this.http.post<any>(url, body, { headers }).pipe(catchError(this.handleError));
      case 'PUT':
        return this.http.put<any>(url, body, { headers }).pipe(catchError(this.handleError));
      case 'DELETE':
        return this.http.delete<any>(url, { headers }).pipe(catchError(this.handleError));
      case 'GET':
      default:
        return this.http.get<any>(url, { headers }).pipe(catchError(this.handleError));
    }
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Unknown error!';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    console.error(`Backend returned code ${error.status}, body was:`, error.error);
    return throwError(() => new Error(errorMessage));
  }
}
