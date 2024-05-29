import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {MatDialog, MatDialogActions, MatDialogContent, MatDialogTitle} from "@angular/material/dialog";
import {MatButton, MatIconButton} from "@angular/material/button";
import {MatFormField, MatInput, MatLabel} from "@angular/material/input";
import {FormsModule} from "@angular/forms";
import {
  MatCell, MatCellDef,
  MatColumnDef,
  MatHeaderCell, MatHeaderCellDef,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef, MatTable, MatTableDataSource
} from "@angular/material/table";
import {MatIcon} from "@angular/material/icon";
import {NgForOf} from "@angular/common";
import {HttpClient} from "@angular/common/http";
import * as yaml from 'js-yaml';

interface ClassData {
  className: string;
  attributeName: string;
  examples: string[];
}

@Component({
  selector: 'app-personalise',
  standalone: true,
  imports: [
    MatDialogActions,
    MatButton,
    MatInput,
    MatLabel,
    MatFormField,
    MatDialogContent,
    MatDialogTitle,
    FormsModule,
    MatRow,
    MatHeaderRow,
    MatHeaderRowDef,
    MatRowDef,
    MatCell,
    MatHeaderCell,
    MatIcon,
    MatColumnDef,
    MatCellDef,
    MatHeaderCellDef,
    MatIconButton,
    NgForOf,
    MatTable
  ],
  templateUrl: './personalise.component.html',
  styleUrl: './personalise.component.scss'
})

export class PersonaliseComponent implements OnInit {
  classData: MatTableDataSource<ClassData> = new MatTableDataSource();
  displayedColumns: string[] = ['className', 'attributeName', 'exampleValues', 'actions'];
  openAPISpec: any;
  @ViewChild('editDialog', { static: true }) editDialog!: TemplateRef<any>;

  constructor(private http: HttpClient, public dialog: MatDialog) {}

  ngOnInit() {
    this.loadOpenAPISpec();
  }

  loadOpenAPISpec() {
    this.http.get('http://localhost:8080/export.yml', { responseType: 'text' }).subscribe((spec: any) => {
      this.openAPISpec = this.parseYAML(spec);
      this.parseOpenAPISpec();
    });
  }

  parseYAML(data: string): any {
    try {
      return yaml.load(data);
    } catch (e) {
      console.error('Failed to parse YAML', e);
      return null;
    }
  }

  parseOpenAPISpec() {
    const schemas = this.openAPISpec.components.schemas;
    const classDataArray: ClassData[] = [];
    for (const className in schemas) {
      if (schemas.hasOwnProperty(className)) {
        const properties = schemas[className].properties;
        for (const attributeName in properties) {
          if (properties.hasOwnProperty(attributeName)) {
            const examples = properties[attributeName].examples || [];
            classDataArray.push({ className, attributeName, examples });
          }
        }
      }
    }
    this.classData.data = classDataArray;
  }

  openEditDialog(className: string, attributeName: string, index: number, example: string) {
    const dialogRef = this.dialog.open(this.editDialog, {
      data: { className, attributeName, index, value: example }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const data = this.classData.data;
        const item = data.find((d: { className: string; attributeName: string; }) => d.className === className && d.attributeName === attributeName);
        if (item) {
          item.examples[index] = result;
          this.classData.data = data;
        }
      }
    });
  }

  saveExampleValues() {
    const updatedValues: any = {};
    this.classData.data.forEach((item: { className: string; attributeName: string; examples: string[]; }) => {
      if (!updatedValues[item.className]) {
        updatedValues[item.className] = {};
      }
      updatedValues[item.className][item.attributeName] = item.examples;
    });

    this.http.post('/update-example-values', updatedValues).subscribe(response => {
      console.log('Example values updated successfully.');
    });
  }

  onDialogCancel() {
    this.dialog.closeAll();
  }

  onDialogSave(className: string, attributeName: string, index: number, value: string) {
    const data = this.classData.data;
    const item = data.find((d: { className: string; attributeName: string; }) => d.className === className && d.attributeName === attributeName);
    if (item) {
      item.examples[index] = value;
      this.classData.data = data;
    }
  }
}
