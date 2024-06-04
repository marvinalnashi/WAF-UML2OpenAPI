import {Component, EventEmitter, Output} from '@angular/core';
import {NgIf} from "@angular/common";
import {MatIconButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {SessionListDialogComponent} from "../session-list-dialog/session-list-dialog.component";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'app-top-bar',
  standalone: true,
  imports: [
    NgIf,
    MatIconButton,
    MatIcon
  ],
  templateUrl: './top-bar.component.html',
  styleUrl: './top-bar.component.scss'
})
export class TopBarComponent {
  @Output() toggleDoc = new EventEmitter<void>();

  constructor(public dialog: MatDialog) {}

  isDarkMode = false;

  toggleTheme() {
    this.isDarkMode = !this.isDarkMode;
    if (this.isDarkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }

  openSessionListDialog(): void {
    const dialogRef = this.dialog.open(SessionListDialogComponent, {
      width: '800px'
    });
  }

  openDocumentation(): void {
    this.toggleDoc.emit();
  }
}
