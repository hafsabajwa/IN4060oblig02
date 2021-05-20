import java.io.PrintWriter;
import java.util.Iterator;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

//create class simpson
public class Simpsons {


	String simPrefix, famPrefix;
	private Model model;
	
	//create object simpson
	public static Simpsons create() {
		return new Simpsons(); //inside object simpson call simpson method
	}
	
	private Simpsons() {  //create simpson method
		model = ModelFactory.createDefaultModel();
	}
	
	
	public Simpsons readFile(String inputFile) {
		FileManager.get().addLocatorClassLoader(Simpsons.class.getClassLoader());
		model = FileManager.get().loadModel(inputFile);
		
		simPrefix = model.getNsPrefixURI("sim");
		famPrefix = model.getNsPrefixURI("fam");
		
		return this;
	}
	//Add information about the simpsons to the model
	
	private Simpsons feedInfo() {
		addPerson("Maggie Simpson", 1);
		Resource mona = addPerson("Mona Simpson", 70);
		Resource abraham = addPerson("Abraham Simpson", 78);
		Resource herb = addPersonName("Herb Simpson");
		
		createMarraige(abraham, mona);
		
		addFatherTo(model.createResource(), herb);
		
		return this;
	}
	//Create marriage between two spouses 
	
	private void createMarraige(Resource spouseOne, Resource spouseTwo) {
		Property spouse = model.createProperty( prefix("hasSpouse", famPrefix) ); 
		spouseOne.addProperty(spouse, spouseTwo);
		spouseTwo.addProperty(spouse, spouseOne);
	}
	
	//add a person with age
	
	private Resource addPerson(String fullName, Integer age) {
		//Create the Simpson
		Resource simpson = addPersonName(fullName);
		
		//Add age to the Simpson
		// link + age
		Property ageProperty = model.createProperty( model.getNsPrefixURI("foaf") + "age" );
		simpson.addProperty(ageProperty, age.toString(), XSDDatatype.XSDint);
		
		return simpson;
	}
	
	// Add a father to child
	 
	
	private void addFatherTo(Resource father, Resource child) {
		Property fatherProp = model.createProperty( prefix("hasFather", famPrefix) );
		child.addProperty(fatherProp, father);
	}
	
	
	
	private Resource addPersonName(String fullName) {
		
		String name = fullName.split(" ")[0];
		

		Resource simpson = model.createResource( prefix(name, simPrefix) );

		simpson.addProperty(RDF.type, FOAF.Person);
		simpson.addProperty(FOAF.name, fullName);
		
		return simpson;
	}
	
	//Writes the model to the given outputFile
	 
	public Simpsons createOutputFile(String outputFile) {
		try (PrintWriter pw = new PrintWriter(outputFile)) {
			model.write(pw, "Turtle");
		} catch (Exception e) {
			System.out.printf("Something went wrong while trying to "
					+ " write to the file: %s", e.getMessage());
		}
		return this;
	}
	
	private String prefix(String name, String prefix) {
		return prefix + name;
	}
	
	private Simpsons setTypesByAge() {
		Property ageProperty = model.createProperty( prefix("age", model.getNsPrefixURI("foaf")) );
		
		//Get all statements for where the subject has an age
		Iterator<Statement> statements = model.listStatements((Resource) null, ageProperty, (Resource) null);
		
		while(statements.hasNext()) {
			Statement statement = statements.next();
			Literal ageLiteral = (Literal) statement.getObject();
			Integer age = ageLiteral.getInt();
			Resource simpson = (Resource) statement.getSubject();
			
			//Check for minors
			setTypesForAge(simpson, age);
		}
		
		return this;
	}
	
	
	private void setTypesForAge(Resource simpson, Integer age) {
		Resource infant = model.createResource( prefix("Infant", famPrefix) );
		Resource minor = model.createResource( prefix("Minor", famPrefix) );
		Resource old = model.createResource( prefix("Old", famPrefix) );
		
		//Check for minors and infants
		if (age < 18) {
			simpson.addProperty(RDF.type, minor);
			
			//If under two it's also an infant
			if (age < 2) {
				simpson.addProperty(RDF.type, infant);
			}
		}
		
		//Check for old people
		if (age > 70) {
			simpson.addProperty(RDF.type, old);
		}
	}
	
	/**
	 * Main method that executes the program
	  
	 * @parameter args
	 */
	public static void main(String[] args) {
		//Error handling
		try {
			Simpsons simpsons = Simpsons.create();
			simpsons.readFile(args[0]); // Input File name
			simpsons.feedInfo();
			simpsons.setTypesByAge();
			simpsons.createOutputFile(args[1]); // Output File name
			System.out.println("File created");
		} catch (Exception e) {
			System.out.println("Something went wrong");
			return;
		}
		

	}
}