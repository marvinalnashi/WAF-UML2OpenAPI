import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatIcon} from "@angular/material/icon";
import {MatIconButton} from "@angular/material/button";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-manage',
  standalone: true,
  imports: [
    MatIcon,
    MatIconButton,
    NgIf
  ],
  templateUrl: './manage.component.html',
  styleUrl: './manage.component.scss'
})
export class ManageComponent {
  @Input() isGeneratedSuccessfully = false;
  @Output() toggleMockServer = new EventEmitter<void>();
  @Output() openSwaggerUI = new EventEmitter<void>();
  @Output() restartApplication = new EventEmitter<void>();
  @Output() downloadOpenAPISpecification = new EventEmitter<void>();

  onToggleMockServer(): void {
    this.toggleMockServer.emit();
  }

  onOpenSwaggerUI(): void {
    this.openSwaggerUI.emit();
  }

  onRestartApplication(): void {
    this.restartApplication.emit();
  }

  onDownloadOpenAPISpecification(): void {
    this.downloadOpenAPISpecification.emit();
  }
}
