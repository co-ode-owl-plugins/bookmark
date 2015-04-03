package org.coode.bookmark;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.util.OWLDataTypeUtils;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.vocab.DublinCoreVocabulary;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Nov 23, 2006<br><br>
 * <p/>
 */
public class OntologyBookmarks {

    private static final String BOOKMARK_PROP = "http://www.co-ode.org/ontologies/meta.owl#bookmark";

    private OWLAnnotationProperty annotationProperty;

    private Set<OWLEntity> bookmarks = new HashSet<OWLEntity>();

    private OWLOntologyManager mngr;

    private OWLOntology ont;

    private Set<OWLDatatype> builtinDatatypes;

    private Set<IRI> builtinAnnotationPropertyIRIs;


    /**
     * @param mngr manager
     * @param ont ontology
     */
    public OntologyBookmarks(OWLOntologyManager mngr, OWLOntology ont) {
        this.mngr = mngr;
        this.ont = ont;

        annotationProperty = mngr.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(BOOKMARK_PROP));

        builtinDatatypes = new OWLDataTypeUtils(mngr).getBuiltinDatatypes();

        builtinAnnotationPropertyIRIs = new HashSet<IRI>();
        builtinAnnotationPropertyIRIs.addAll(OWLRDFVocabulary.BUILT_IN_ANNOTATION_PROPERTY_IRIS);
        for (IRI uri : DublinCoreVocabulary.ALL_URIS){
            builtinAnnotationPropertyIRIs.add(uri);
        }

        loadAnnotations();
    }

    /**
     * @return the ontology
     */
    public OWLOntology getOntology(){
        return ont;
    }

    /**
     * @return a copy of the bookmarks
     */
    public Set<OWLEntity> getBookmarks(){
        return Collections.unmodifiableSet(bookmarks);
    }

    /**
     * @return number of bookmarks
     */
    public int getSize() {
        return bookmarks.size();
    }

    /**
     * @param obj object to add
     * @return changes to apply
     */
    public List<OWLOntologyChange> add(OWLEntity obj) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        if (bookmarks.add(obj)){
            OWLLiteral value = mngr.getOWLDataFactory().getOWLLiteral(
                    obj.getIRI().toString());
            OWLAnnotation annot = mngr.getOWLDataFactory().getOWLAnnotation(annotationProperty, value);
            changes.add(new AddOntologyAnnotation(ont, annot));
        }
        changes.addAll(tidyOldStyleAnnotations());
        return changes;
    }

    /**
     * @param obj object to remove
     * @return changes to apply
     */
    public List<OWLOntologyChange> remove(OWLEntity obj) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        if (bookmarks.remove(obj)){
            for (OWLAnnotation annotation : ont.getAnnotations()){
                if (annotation.getProperty().equals(annotationProperty)){
                    final OWLAnnotationValue annotationValue = annotation.getValue();
                    if (annotationValue instanceof OWLLiteral){
                        OWLLiteral literal = (OWLLiteral)annotationValue;
                        if (literal.getLiteral().equals(obj.getIRI().toString())){
                            changes.add(new RemoveOntologyAnnotation(ont, annotation));
                        }
                    }
                }
            }
        }
        changes.addAll(tidyOldStyleAnnotations());
        return changes;
    }


    /**
     * Get rid of any annotations that contain more than one bookmark. Replace them with individual ones
     * @return a list of changes required to split these annotations down into separate ones
     */
    private List<OWLOntologyChange> tidyOldStyleAnnotations() {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for (OWLAnnotation annotation : ont.getAnnotations()){
            if (annotation.getProperty().equals(annotationProperty)){
                final OWLAnnotationValue annotationValue = annotation.getValue();
                if (annotationValue instanceof OWLLiteral){
                    String[] values = ((OWLLiteral)annotationValue).getLiteral().split("\n");
                    if (values.length > 1){
                        changes.add(new RemoveOntologyAnnotation(ont, annotation));
                        for (String v : values){
                            for (OWLEntity bookmark : bookmarks){
                                if (bookmark.getIRI().toString().equals(v)){
                                    OWLLiteral literal = mngr
                                            .getOWLDataFactory().getOWLLiteral(
                                                    v);
                                    OWLAnnotation annot = mngr.getOWLDataFactory().getOWLAnnotation(annotationProperty, literal);
                                    changes.add(new AddOntologyAnnotation(ont, annot));
                                }
                            }
                        }
                    }
                }
            }
        }
        return changes;
    }


    private void loadAnnotations() {
        // load the bookmark from the ontology annotations
        for (OWLAnnotation annotation : ont.getAnnotations()){
            if (annotation.getProperty().equals(annotationProperty)){
                OWLAnnotationValue content = annotation.getValue();
                if (content instanceof OWLLiteral){
                    parseAnnotation(((OWLLiteral)content).getLiteral());
                }
            }
        }
    }

    private void parseAnnotation(String s) {
        for (String value : s.split("\n")){
            try {
                IRI iri = IRI.create(new URI(value));
                OWLEntity e = getEntityFromIRI(iri);
                if (e != null){
                    bookmarks.add(e);
                }
            }
            catch (URISyntaxException e) {
                Logger.getLogger(BookmarkModel.class).error(e);
            }
        }
    }

    private OWLEntity getEntityFromIRI(IRI iri) {
        for (OWLOntology ontology : getOntologies()){
            if (ontology.containsClassInSignature(iri, Imports.EXCLUDED)) {
                return mngr.getOWLDataFactory().getOWLClass(iri);
            }

            if (ontology.containsObjectPropertyInSignature(iri, Imports.EXCLUDED)) {
                return mngr.getOWLDataFactory().getOWLObjectProperty(iri);
            }

            if (ontology.containsDataPropertyInSignature(iri, Imports.EXCLUDED)) {
                return mngr.getOWLDataFactory().getOWLDataProperty(iri);
            }

            if (ontology.containsIndividualInSignature(iri, Imports.EXCLUDED)) {
                return mngr.getOWLDataFactory().getOWLNamedIndividual(iri);
            }

            if (builtinAnnotationPropertyIRIs.contains(iri)
                    || ontology.containsAnnotationPropertyInSignature(iri,
                            Imports.EXCLUDED)) {
                return mngr.getOWLDataFactory().getOWLAnnotationProperty(iri);
            }

            // check datatypes including standard ones that are not currently used
            OWLDatatype dt = mngr.getOWLDataFactory().getOWLDatatype(iri);
            if (builtinDatatypes.contains(dt)
                    || ontology.containsDatatypeInSignature(iri, Imports.EXCLUDED)) {
                return dt;
            }
        }

        return null;
    }

    private Set<OWLOntology> getOntologies() {
        return mngr.getOntologies(); // should be active ontologies
    }
}
