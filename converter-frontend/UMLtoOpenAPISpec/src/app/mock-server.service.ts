import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MockServerService {

  private baseUrl = 'http://localhost:8080';

  toggleMockServer(): Observable<any> {
    return this.http.get(`${this.baseUrl}/toggle-prism-mock`);
  }
  private backendUrl = 'http://localhost:8080';
  private mockServerUrl = 'http://localhost:4010';

  constructor(private http: HttpClient) { }

  testOpenApiSpecification(): Observable<any> {
    return this.http.get(`${this.backendUrl}/test-openapi`)
      .pipe(
        catchError(this.handleError)
      );
  }

  testApi(method: string, path: string, body?: any): Observable<any> {
    const url = `${this.mockServerUrl}${path}`;
    switch (method.toLowerCase()) {
      case 'get':
        return this.http.get(url)
          .pipe(
            catchError(this.handleError)
          );
      case 'post':
        return this.http.post(url, body)
          .pipe(
            catchError(this.handleError)
          );
      case 'put':
        return this.http.put(url, body)
          .pipe(
            catchError(this.handleError)
          );
      case 'delete':
        return this.http.delete(url)
          .pipe(
            catchError(this.handleError)
          );
      default:
        throw new Error(`Unsupported HTTP method: ${method}`);
    }
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred!';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `A network error occurred: ${error.error.message}`;
    } else {
      errorMessage = `Backend returned code ${error.status}, body was: ${JSON.stringify(error.error)}`;
    }
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
