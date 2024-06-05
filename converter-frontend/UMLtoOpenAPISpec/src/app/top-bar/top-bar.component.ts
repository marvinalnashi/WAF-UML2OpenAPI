import { Component } from '@angular/core';
import {NgIf} from "@angular/common";
import {MatIconButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {SessionListDialogComponent} from "../session-list-dialog/session-list-dialog.component";
import {MatDialog} from "@angular/material/dialog";

/**
 * Component for displaying the top bar of the application and initialising its icons.
 */
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

  /**
   * Indicates whether the dark mode is enabled.
   */
  isDarkMode = false;

  /**
   * Creates an instance of TopBarComponent.
   * @param dialog The popup dialog service.
   */
  constructor(public dialog: MatDialog) {}

  /**
   * Toggles the theme of the application between light mode and dark mode.
   */
  toggleTheme() {
    this.isDarkMode = !this.isDarkMode;
    if (this.isDarkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }

  /**
   * Opens the Session List popup dialog after its corresponding icon in the top bar has been clicked.
   */
  openSessionListDialog(): void {
    const dialogRef = this.dialog.open(SessionListDialogComponent, {
      width: '600px'
    });
  }
}
