import org.coode.owlapi.functionalrenderer.OWLFunctionalSyntaxOntologyStorer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class CoreParser {

    private static String RIM = "http://org.hl7.v3/rim/core#";
    private static String UPP = "http://org.hl7.v3/rim/upper#";
    private static String DAT = "urn:hl7-org:cdsdt:r2#";

    public static void main( String... args ) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException {
        File src = new File( "/home/davide/Projects/Git/Mayo/hl7_rim/RIM-Model/src/main/resources/owl/attrib-souce.txt" );
        File tgt = new File( "/home/davide/Projects/Git/Mayo/hl7_rim/RIM-Model/src/main/resources/owl/rim-core.owl" );

        OWLOntology ontology;
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        ontology = manager.createOntology( new OWLOntologyID(
                IRI.create( RIM ),
                IRI.create( RIM.replace( "#", "/1.0#" ) ) ) );

        OWLFunctionalSyntaxOntologyFormat pfm = new OWLFunctionalSyntaxOntologyFormat();
        pfm.setPrefix( "dt", DAT );
        pfm.setPrefix( "core", RIM );
        pfm.setPrefix( "upper", UPP );

        manager.applyChange( new AddImport( ontology, factory.getOWLImportsDeclaration( IRI.create( UPP ) ) ) );

        BufferedReader br = new BufferedReader( new FileReader( src ) );
        String line;
        while ( (line = br.readLine()) != null ) {
            if ( line.contains( "::" ) ) {
                addSubClass( line, ontology, factory );
            } else {
                addAttribute( line, ontology, factory );
            }
        }

        fixPropertyDomain( ontology, factory );

        OWLOntologyStorer storer = new OWLFunctionalSyntaxOntologyStorer();
        storer.storeOntology( ontology, IRI.create( tgt ), pfm );
    }

    private static void fixPropertyDomain( OWLOntology ontology, OWLDataFactory factory ) {
        Map<OWLObjectPropertyExpression, Object> doms = new HashMap<>();
        Set<OWLObjectPropertyExpression> changes = new HashSet<>(  );
        for ( OWLObjectPropertyDomainAxiom dom : ontology.getAxioms( AxiomType.OBJECT_PROPERTY_DOMAIN ) ) {
            if ( doms.containsKey( dom.getProperty() ) ) {
                Object d = doms.get( dom.getProperty() );
                if ( d instanceof OWLClassExpression ) {
                    doms.remove( dom.getProperty() );
                    Set<OWLClassExpression> exprs = new HashSet<>(  );
                    exprs.add( (OWLClassExpression) d );
                    exprs.add( dom.getDomain() );
                    changes.add( dom.getProperty() );
                    doms.put( dom.getProperty(), exprs );
                } else if( d instanceof Set ) {
                    ( (Set) d ).add( dom.getDomain() );
                }
            } else {
                doms.put( dom.getProperty(), dom.getDomain() );
            }
        }
        for ( OWLObjectPropertyExpression expr : changes ) {
            for ( OWLPropertyDomainAxiom domax : ontology.getObjectPropertyDomainAxioms( expr ) ) {
                ontology.getOWLOntologyManager().applyChanges( ontology.getOWLOntologyManager().removeAxiom( ontology, domax ) );
            }
            ontology.getOWLOntologyManager().applyChanges( ontology.getOWLOntologyManager().addAxiom( ontology,
                                                                                                      factory.getOWLObjectPropertyDomainAxiom( expr, factory.getOWLObjectUnionOf( (Set) doms.get( expr ) ) ) ) );
        }
        System.out.print( "" );
    }

    private static void addAttribute( String line, OWLOntology ontology, OWLDataFactory factory ) {
        StringTokenizer tok = new StringTokenizer( line, ":" );
        String dom = tok.nextToken();
        String rel = tok.nextToken();
        String ran = tok.nextToken();
        boolean single = "1".equals( tok.nextToken() );

        OWLObjectProperty prop = factory.getOWLObjectProperty( IRI.create( RIM + rel ) );
        OWLAxiom axiom;

        axiom = factory.getOWLObjectPropertyDomainAxiom( prop, factory.getOWLClass( IRI.create( RIM + dom ) ) );
        ontology.getOWLOntologyManager().applyChanges( ontology.getOWLOntologyManager().addAxiom( ontology, axiom ) );

        axiom = factory.getOWLObjectPropertyRangeAxiom( prop, factory.getOWLClass( IRI.create( DAT + ran ) ) );
        ontology.getOWLOntologyManager().applyChanges( ontology.getOWLOntologyManager().addAxiom( ontology, axiom ) );

        if ( single ) {
            axiom = factory.getOWLFunctionalObjectPropertyAxiom( prop );
            ontology.getOWLOntologyManager().applyChanges( ontology.getOWLOntologyManager().addAxiom( ontology, axiom ) );
        }

        if ( single ) {
            axiom = factory.getOWLSubClassOfAxiom( factory.getOWLClass( IRI.create( RIM + dom ) ),
                                                   factory.getOWLObjectMaxCardinality( 1, prop, factory.getOWLClass( IRI.create( DAT + ran ) ) ) );
            ontology.getOWLOntologyManager().applyChanges( ontology.getOWLOntologyManager().addAxiom( ontology, axiom ) );
        }
    }

    private static void addSubClass( String line, OWLOntology ontology, OWLDataFactory factory ) {
        StringTokenizer tok = new StringTokenizer( line, "::" );
        String sub = tok.nextToken();
        String sup = tok.nextToken();
        OWLSubClassOfAxiom ska = factory.getOWLSubClassOfAxiom( factory.getOWLClass( IRI.create( RIM + sub ) ),
                                                                factory.getOWLClass( IRI.create( ( isUpp( sup ) ? UPP : RIM ) + sup ) ) );
        ontology.getOWLOntologyManager().applyChanges( ontology.getOWLOntologyManager().addAxiom( ontology, ska ) );
    }

    private static boolean isUpp( String sup ) {
        return "TypedRole".equals( sup ) || "ActuationAct".equals( sup ) || "ObservationAct".equals( sup ) || "TypedEntity".equals( sup );
    }
}
