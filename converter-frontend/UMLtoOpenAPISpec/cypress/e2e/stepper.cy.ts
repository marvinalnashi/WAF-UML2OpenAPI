beforeEach(() => {
  cy.visit('/');
  cy.wait(2500);
});

describe('Basic stepper end-to-end tests', () => {
  it('Start step', () => {
    cy.contains('Start').should('be.visible');
    cy.contains('Welcome to the WeAreFrank! UML diagram to OpenAPI specification converter application!').should('be.visible');
  });

  it('Top bar: check previous sessions popup', () => {
    cy.get('button').eq(1).click();
    cy.get('div.mdc-dialog__container', { timeout: 10000 }).should('have.css', 'opacity', '1');
    cy.contains('Previous Sessions').should('be.visible');
  });

  it('should change the color of the top bar when toggling light/dark mode', () => {
    cy.get('.top-bar').invoke('css', 'background-color').then((initialColor) => {
      cy.get('button').eq(0).click();
      cy.wait(1000);
      cy.get('.top-bar').invoke('css', 'background-color').should((newColor) => {
        expect(newColor).to.not.equal(initialColor);
      });
    });
  });
});

describe('Navigational flow stepper end-to-end tests', () => {

  beforeEach(() => {
    cy.get('.btn').eq(0).click();
    cy.contains('Select a file').click();
    const fileName = 'testumldiagram.uxf';
    cy.get('input[type="file"]').attachFile(fileName);
    cy.get('input[type="file"]').then(input => {
      const fileInput = input[0] as HTMLInputElement;
      if (fileInput.files && fileInput.files.length > 0) {
        cy.get('.btn').contains('Next').should('be.visible').click();
      }
    });
  });

  it('Upload step: uploading a file', () => {
    cy.contains('Add Class').should('be.visible');
  });

  it('Mapping step: adding a class', () => {
    cy.addClassWithMethodAndAttribute();
    cy.get('.table-container').scrollTo('right');
    cy.get('table').contains('Plant').should('be.visible');
  });

  it('Mapping step: removing a class', () => {
    cy.removeClassElements();
    cy.get('.table-container').scrollTo('right');
    cy.get('table').contains('th', 'Plant').should('not.exist');
  });

  it('Generate step: generating an OpenAPI specification', () => {
    cy.generateOpenAPISpecification();
    cy.get('button').contains('Plant', { timeout: 40000}).should('be.visible');
  });

  it('Personalise step: renaming example values', () => {
    cy.renameExampleValues();
    cy.get('div').contains('cactus');
  });

  it('Manage step: start Prism mock server', () => {
    cy.startMockServer();
  });

  it('Manage step: save session data and restart stepper', () => {
    cy.saveSessionAndRestartStepper();
  });

  it('Manage step: download generated OpenAPI specification', () => {
    cy.downloadOpenAPISpecification();
  });
});
