import 'cypress-file-upload';

Cypress.Commands.add('addClassWithMethodAndAttribute', () => {
  cy.contains('Add Class').should('be.visible').click();

  cy.get('input[placeholder="Class Name"]').should('be.visible').type('Plant');
  cy.get('input[placeholder="Path"]').should('be.visible').type('plant');

  cy.contains('Add Method').should('be.visible').click();
  cy.wait(500);
  cy.get('input[formControlName="name"]').should('be.visible').type('checkPlantHealth');
  cy.get('input[formControlName="parameters"]').should('be.visible').type('plantName : String, plantAge : int');
  cy.get('mat-select[formControlName="dataType"]').click();
  cy.get('mat-option').contains('void').click();
  cy.wait(500);
  cy.get('button[cdkFocusInitial]').contains('Add').click();

  cy.contains('Add Attribute').should('be.visible').click();
  cy.get('mat-select[formControlName="accessModifier"]').click();
  cy.get('mat-option').contains('Private (-)').click();
  cy.get('input[formControlName="name"]').should('be.visible').type('plantType');
  cy.get('mat-select[formControlName="dataType"]').click();
  cy.get('mat-option').contains('String').click();
  cy.get('button[cdkFocusInitial]').contains('Add').click();

  cy.contains('Apply Additions').should('be.visible').click();

  cy.contains('Manage Elements').should('be.visible').click();
});

Cypress.Commands.add('removeClassElements', () => {
  cy.addClassWithMethodAndAttribute();

  cy.get('.table-container').scrollTo('right');
  cy.get('table').contains('th', 'Plant').then($th => {
    cy.wrap($th).parent().within(() => {
      cy.get('button').eq(6).contains('🗑️').click();
    });
  });
});

Cypress.Commands.add('generateOpenAPISpecification', () => {
  cy.addClassWithMethodAndAttribute();
  cy.get('button').contains('Continue').click();
  cy.get('button').contains('Generate').click();
  cy.get('#continueButtonAfterGenerate', { timeout: 60000 }).should('be.visible').click();
});

Cypress.Commands.add('renameExampleValues', () => {
  cy.generateOpenAPISpecification();
  cy.get('button').contains('Plant').click();
  cy.get('button').contains('plantType').click();
  cy.get('#exampleValue').parent().find('mat-icon').eq(1).click();
  cy.get('input#renameValue').should('be.visible')
    .clear({ force: true })
    .type('cactus', { force: true });
  cy.get('button').contains('Save').click();
});

Cypress.Commands.add('startMockServer', () => {
  cy.generateOpenAPISpecification();
  cy.get('#continueButtonAfterPersonalise').should('be.visible').click();
  cy.get('#startMockServerButton').should('be.visible').click();
  cy.get('.toast-success').contains('The Prism mock server has successfully started.').should('be.visible');
});

Cypress.Commands.add('saveSessionAndRestartStepper', () => {
  cy.generateOpenAPISpecification();
  cy.get('#continueButtonAfterPersonalise').should('be.visible').click();
  cy.get('#saveSessionRestartStepperButton').should('be.visible').click();
  cy.contains('Welcome to the WeAreFrank! UML diagram to OpenAPI specification converter application!').should('be.visible');
});

Cypress.Commands.add('downloadOpenAPISpecification', () => {
  cy.generateOpenAPISpecification();
  cy.get('#continueButtonAfterPersonalise').should('be.visible').click();
  cy.get('#downloadOpenAPISpecificationButton').should('be.visible').click();
  cy.get('#download-dialog-proceed-btn').should('be.visible').click();
  cy.get('.toast-success').contains('The generated OpenAPI specification has been downloaded successfully.').should('be.visible');
});
