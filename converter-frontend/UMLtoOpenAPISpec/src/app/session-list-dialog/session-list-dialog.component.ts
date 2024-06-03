import {Component, OnInit} from '@angular/core';
import {StepperSessionService} from "../stepper-session.service";
import {
  MatCell, MatCellDef,
  MatColumnDef,
  MatHeaderCell, MatHeaderCellDef,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef, MatTable
} from "@angular/material/table";
import {MatDialog, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from "@angular/material/dialog";
import {MatButton, MatIconButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {DatePipe} from "@angular/common";

@Component({
  selector: 'app-session-list-dialog',
  standalone: true,
  imports: [
    MatHeaderRow,
    MatHeaderRowDef,
    MatRow,
    MatRowDef,
    MatDialogActions,
    MatButton,
    MatDialogClose,
    MatCell,
    MatHeaderCell,
    MatColumnDef,
    MatCellDef,
    MatHeaderCellDef,
    MatTable,
    MatDialogContent,
    MatDialogTitle,
    MatIcon,
    MatIconButton,
    DatePipe
  ],
  templateUrl: './session-list-dialog.component.html',
  styleUrl: './session-list-dialog.component.scss'
})
export class SessionListDialogComponent implements OnInit {
  sessions: any[] = [];
  displayedColumns: string[] = ['createdAt', 'umlDiagram', 'openApiSpec', 'actions'];

  constructor(private sessionService: StepperSessionService, public dialog: MatDialog) {}

  ngOnInit(): void {
    this.sessionService.getAllSessions().subscribe((data: any[]) => {
      this.sessions = data;
    });
  }

  downloadSession(session: any) {
    this.sessionService.getSession(session.id).subscribe((data) => {
      const umlBlob = new Blob([data.umlDiagram], { type: 'text/xml' });
      const openApiBlob = new Blob([data.openApiSpec], { type: 'application/json' });

      const umlUrl = window.URL.createObjectURL(umlBlob);
      const openApiUrl = window.URL.createObjectURL(openApiBlob);

      const umlLink = document.createElement('a');
      umlLink.href = umlUrl;
      umlLink.download = `uml-diagram-${session.id}.xml`;
      document.body.appendChild(umlLink);
      umlLink.click();
      document.body.removeChild(umlLink);

      const openApiLink = document.createElement('a');
      openApiLink.href = openApiUrl;
      openApiLink.download = `openapi-spec-${session.id}.json`;
      document.body.appendChild(openApiLink);
      openApiLink.click();
      document.body.removeChild(openApiLink);
    });
  }
}
