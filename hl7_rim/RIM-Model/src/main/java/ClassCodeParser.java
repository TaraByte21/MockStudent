import org.coode.owlapi.functionalrenderer.OWLFunctionalSyntaxOntologyStorer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLOntologyStorer;
import org.semanticweb.owlapi.model.OWLPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class ClassCodeParser {

    private static String RIM = "http://org.hl7.v3/rim/core#";
    private static String EXT = "http://org.hl7.v3/rim/ext#";
    private static String CLS = "http://org.hl7.v3/rim/classes#";
    private static String UPP = "http://org.hl7.v3/rim/upper#";
    private static String DAT = "urn:hl7-org:cdsdt:r2#";


    private static Map<String,Descr> registry = new HashMap<>();
    private static Set<String> classNames = new HashSet<>();

    public static void main( String... args ) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException {
        File src = new File( "/home/davide/Projects/Git/Mayo/hl7_rim/RIM-Model/src/main/resources/owl/class-source.txt" );
        File core = new File( "/home/davide/Projects/Git/Mayo/hl7_rim/RIM-Model/src/main/resources/owl/rim-core.owl" );
        File upper = new File( "/home/davide/Projects/Git/Mayo/hl7_rim/RIM-Model/src/main/resources/owl/rim-upper.owl" );
        File dt = new File( "/home/davide/Projects/Git/Mayo/hl7_rim/RIM-Model/src/main/resources/owl/datatype.owl" );
        File tgt = new File( "/home/davide/Projects/Git/Mayo/hl7_rim/RIM-Model/src/main/resources/owl/rim-classes.owl" );

        OWLOntology ontology;
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        ontology = manager.createOntology( new OWLOntologyID(
                IRI.create( CLS ),
                IRI.create( CLS.replace( "#", "/1.0#" ) ) ) );
        manager.loadOntologyFromOntologyDocument( new FileInputStream( dt ) );
        manager.loadOntologyFromOntologyDocument( new FileInputStream( upper ) );
        manager.loadOntologyFromOntologyDocument( new FileInputStream( core ) );

        OWLFunctionalSyntaxOntologyFormat pfm = new OWLFunctionalSyntaxOntologyFormat();
        pfm.setPrefix( "core", RIM );
        pfm.setPrefix( "cls", CLS );
        pfm.setPrefix( "dt", DAT );
        pfm.setPrefix( "upper", UPP );
        pfm.setPrefix( "ext", EXT );

        manager.applyChange( new AddImport( ontology, factory.getOWLImportsDeclaration( IRI.create( RIM ) ) ) );

        BufferedReader br = new BufferedReader( new FileReader( src ) );
        String line;
        while ( (line = br.readLine()) != null ) {
            registerClass( line, ontology );
        }
        processDiscoveredClasses( registry, ontology, factory );

        OWLOntologyStorer storer = new OWLFunctionalSyntaxOntologyStorer();
        storer.storeOntology( ontology, IRI.create( tgt ), pfm );
    }

    private static void processDiscoveredClasses( Map<String, Descr> registry, OWLOntology ontology, OWLDataFactory factory ) {
        for ( String key : registry.keySet() ) {
            Descr d = registry.get( key );

            OWLClass klass = factory.getOWLClass( IRI.create( ( d.namespace + d.className ) ) );

            if ( ! UPP.equals( d.namespace ) ) {
                addAxiom( ontology, factory.getOWLDeclarationAxiom( klass ) );
            }

            if ( d.target != null ) {

                Descr parent = registry.get( d.kind + d.target );
                OWLClass superClass = factory.getOWLClass( IRI.create( ( parent.namespace + parent.className ) ) );
                OWLSubClassOfAxiom subk = factory.getOWLSubClassOfAxiom( klass,superClass );
                addAxiom( ontology, subk );

                OWLEquivalentClassesAxiom ekk = factory.getOWLEquivalentClassesAxiom( klass,
                                                                                      factory.getOWLObjectIntersectionOf(
                                                                                              factory.getOWLClass( IRI.create( UPP + "Typed" + d.kind ) ),
                                                                                              factory.getOWLObjectSomeValuesFrom(
                                                                                                      factory.getOWLObjectProperty( IRI.create( DAT + "classCode" ) ),
                                                                                                      factory.getOWLObjectIntersectionOf(
                                                                                                              factory.getOWLClass( IRI.create( DAT + "CS" ) ),
                                                                                                              factory.getOWLDataHasValue(
                                                                                                                      factory.getOWLDataProperty( IRI.create( DAT + "code" ) ),
                                                                                                                      factory.getOWLLiteral( d.code, OWL2Datatype.XSD_STRING )
                                                                                                              )
                                                                                                      )
                                                                                              )
                                                                                      ) );
                addAxiom( ontology, ekk );
            }

        }
    }

    private static void addAxiom( OWLOntology ontology, OWLAxiom axiom ) {
        ontology.getOWLOntologyManager().applyChanges( ontology.getOWLOntologyManager().addAxiom( ontology,
                                                                                                  axiom ) );

    }


    private static void registerClass( String line, OWLOntology ontology ) {
        Descr d = new Descr();
        StringTokenizer tok = new StringTokenizer( line, ":" );
        boolean hasName = tok.countTokens() == 6;

        if ( line.endsWith( ":ACT" ) ) {
            d.kind = "Act";
            d.className = "TypedAct";
            d.code = "ACT";
            d.namespace = UPP;
        } else if ( line.endsWith( ":ROL" ) ) {
            d.kind = "Role";
            d.className = "TypedRole";
            d.code = "ROL";
            d.namespace = UPP;
        } else if ( line.endsWith( ":ENT" ) ) {
            d.kind = "Entity";
            d.className = "TypedEntity";
            d.code = "ENT";
            d.namespace = UPP;
        } else {
            d.kind = tok.nextToken();
            if ( hasName ) {
                d.className = camelcase( tok.nextToken() );
            }
            d.printName = tok.nextToken();
            d.rel = tok.nextToken();
            d.target = tok.nextToken();
            d.code = tok.nextToken();
            if ( existsInCore( d, ontology ) ) {
                d.namespace = RIM;
            } else {
                d.namespace = EXT;
            }

            if ( d.className == null || "".equals( d.className ) ) {
                d.className = camelcase( d.printName );
            }
        }
        registry.put( d.kind + d.code, d );
        if ( classNames.contains( d.className ) ) {
            d.className = d.className + "_";
        }
        classNames.add( d.className );
    }

    private static boolean existsInCore( Descr d, OWLOntology ontology ) {
        OWLClass candidate = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass( IRI.create( RIM + d.className ) );
        if ( ! ontology.getClassesInSignature( true ).contains( candidate ) ) {
            return false;
        }
        // if present, it may still be an alias. we need to navigate to the root of hierarchy
        OWLClass parent = candidate;
        do {
            parent = lookForParent( parent, ontology );
            if ( parent != null && parent.getIRI().getFragment().endsWith( d.kind ) ) {
                return true;
            }
        } while ( parent != null );
        return false;
    }

    private static OWLClass lookForParent( OWLClass parent, OWLOntology ontology ) {
        for ( OWLSubClassOfAxiom subk : ontology.getOWLOntologyManager().getOntology( IRI.create( RIM ) ).getSubClassAxiomsForSubClass( parent ) ) {
            if ( ! subk.getSuperClass().isAnonymous() ) {
                return subk.getSuperClass().asOWLClass();
            }
        }
        return null;
    }

    private static String camelcase( String printName ) {
        StringTokenizer tok = new StringTokenizer( printName );
        StringBuilder builder = new StringBuilder();
        while ( tok.hasMoreTokens() ) {
            builder.append( toUpperCase( tok.nextToken() ) );
        }
        return builder.toString().replaceAll( "\'", "" );
    }

    private static String toUpperCase( String s ) {
        return s.substring( 0, 1 ).toUpperCase() + s.substring( 1 );
    }


    private static class Descr {
        public String kind;
        public String className;
        public String printName;
        public String rel;
        public String target;
        public String code;
        public String namespace;
    }
}
