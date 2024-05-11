import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class FileService {
  private file: File | null = null;
  private diagramDetails: any = null;

  setFile(file: File) {
    this.file = file;
  }

  getFile(): File | null {
    return this.file;
  }

  setDiagramDetails(details: any) {
    this.diagramDetails = details;
  }

  getDiagramDetails() {
    return this.diagramDetails;
  }
}
