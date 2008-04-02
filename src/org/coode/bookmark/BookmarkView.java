package org.coode.bookmark;

import org.apache.log4j.Logger;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.view.AbstractOWLSelectionViewComponent;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLObject;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;

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
 * Date: Oct 5, 2006<br><br>
 * <p/>
 */
public class BookmarkView extends AbstractOWLSelectionViewComponent {

    private BookmarkList list;

    private DisposableAction deleteAction = new DisposableAction("Remove Bookmark", OWLIcons.getIcon("class.delete.png")){
        public void dispose() {
        }

        public void actionPerformed(ActionEvent actionEvent) {
            for (OWLObject obj : list.getSelectedObjects()){
                try {
                    if (obj instanceof OWLEntity){
                        list.getBookmarkModel().remove((OWLEntity)obj);
                    }
                }
                catch (OWLException e) {
                    Logger.getLogger(BookmarkView.class).error(e);
                }
            }
        }
    };

    private ListSelectionListener listSelectionListener = new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
            final Object val = list.getSelectedValue();
            deleteAction.setEnabled(val != null);
            if (val != null){
                getOWLEditorKit().getOWLWorkspace().getOWLSelectionModel().setSelectedEntity((OWLEntity)val);
            }
        }
    };

    public void initialiseView() throws Exception {
        setLayout(new BorderLayout());

        list = new BookmarkList(getOWLEditorKit());

        list.getSelectionModel().addListSelectionListener(listSelectionListener);

        add(new JScrollPane(list), BorderLayout.CENTER);

        deleteAction.setEnabled(false);

        addAction(deleteAction, "A", "A");
    }

    public void disposeView() {
        list.getSelectionModel().removeListSelectionListener(listSelectionListener);
        list.getBookmarkModel().dispose();
    }

    protected OWLObject updateView() {
        OWLEntity selectedEntity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
        if (list.getSelectedObjects().contains(selectedEntity)){
            return selectedEntity;
        }
        else if (list.getBookmarkModel().contains(selectedEntity)){
            list.setSelectedValue(selectedEntity, true);
            return selectedEntity;
        }
        list.clearSelection();
        return null;
    }


    protected boolean isOWLClassView() {
        return true;
    }


    protected boolean isOWLObjectPropertyView() {
        return true;
    }


    protected boolean isOWLDataPropertyView() {
        return true;
    }


    protected boolean isOWLIndividualView() {
        return true;
    }
}
