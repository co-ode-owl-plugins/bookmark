package org.coode.bookmark;

import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.list.OWLObjectList;
import org.protege.editor.owl.ui.table.OWLObjectDropTargetListener;
import org.protege.editor.owl.ui.transfer.OWLObjectDropTarget;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLRuntimeException;

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
 * nick.drummond@cs.manchester.ac.uk<br>
 * http://www.cs.man.ac.uk/~drummond<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Oct 5, 2006<br><br>
 * <p/>
 */
public class BookmarkList extends OWLObjectList<OWLEntity> implements OWLObjectDropTarget {
    private static final long serialVersionUID = 1L;

    private final OWLEditorKit eKit;
    private final BookmarkModel model;

    /**
     * @param owlEditorKit editor kit for the component
     */
    public BookmarkList(OWLEditorKit owlEditorKit) {
        super(owlEditorKit);

        eKit = owlEditorKit;

        model = new BookmarkModel(owlEditorKit.getModelManager());
        setModel(model);

        // Variable is unised but calling the constructor is necessary to register the target listener
        @SuppressWarnings("unused")
        DropTarget dropTarget = new DropTarget(this, new OWLObjectDropTargetListener(this));
    }

    public JComponent getComponent() {
        return this;
    }

    public boolean dropOWLObjects(List<OWLObject> owlObjects, Point pt, int type) {
        for (OWLObject obj: owlObjects){
            try {
                if (obj instanceof OWLEntity){
                    model.add((OWLEntity)obj);
                }
            }
            catch (OWLRuntimeException e) {
                Logger.getLogger(BookmarkList.class).error(e);
            }
        }
        return true;
    }

    public OWLModelManager getOWLModelManager() {
        return eKit.getModelManager();
    }

    /**
     * @return bookmark model
     */
    public BookmarkModel getBookmarkModel() {
        return model;
    }

    /**
     * @return currently selected objects
     */
    public Set<OWLEntity> getSelectedObjects() {
        Set<OWLEntity> objs = new HashSet<OWLEntity>();
        for (int row : getSelectedIndices()){
            objs.add(getModel().getElementAt(row));
        }
        return objs;
    }
}
