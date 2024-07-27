import {Component, Inject} from '@angular/core';
import {MockServerService} from "../mock-server.service";
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from "@angular/material/dialog";
import {JsonPipe, NgForOf, NgIf} from "@angular/common";
import {MatButton} from "@angular/material/button";

@Component({
  selector: 'app-test-openapi-dialog',
  standalone: true,
  imports: [
    MatDialogTitle,
    MatDialogContent,
    NgIf,
    MatButton,
    MatDialogActions,
    JsonPipe,
    NgForOf
  ],
  templateUrl: './test-openapi-dialog.component.html',
  styleUrl: './test-openapi-dialog.component.scss'
})
export class TestOpenApiDialogComponent {
  testResults: any[] = [];

  constructor(
    public dialogRef: MatDialogRef<TestOpenApiDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private mockServerService: MockServerService
  ) {}

  onCloseClick(): void {
    this.dialogRef.close();
  }

  onTestClick(): void {
    this.mockServerService.testOpenApiSpecification().subscribe(results => {
      this.testResults = results;
    }, error => {
      console.error('Error testing OpenAPI specification', error);
    });
  }
}
