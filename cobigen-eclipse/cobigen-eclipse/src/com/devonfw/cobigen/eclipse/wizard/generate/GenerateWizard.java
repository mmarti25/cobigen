package com.devonfw.cobigen.eclipse.wizard.generate;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devonfw.cobigen.eclipse.common.constants.external.CobiGenDialogConstants;
import com.devonfw.cobigen.eclipse.generator.CobiGenWrapper;
import com.devonfw.cobigen.eclipse.generator.java.JavaInputGeneratorWrapper;
import com.devonfw.cobigen.eclipse.wizard.common.SelectFilesPage;
import com.devonfw.cobigen.eclipse.wizard.generate.common.AbstractGenerateWizard;
import com.devonfw.cobigen.eclipse.wizard.generate.common.SelectAttributesPage;
import com.devonfw.cobigen.eclipse.wizard.generate.control.GenerateSelectionJob;

/**
 * The {@link SelectFilesPage} guides through the generation process
 */
public class GenerateWizard extends AbstractGenerateWizard {

    /** The second page of the Wizard */
    private SelectAttributesPage page2;

    /** Assigning logger to GenerateWizard */
    private static final Logger LOG = LoggerFactory.getLogger(GenerateWizard.class);

    /** Job to build page 2 */
    private Job buildPage2Job;

    /**
     * The {@link GenerateWizard} guides through the generation process
     * @param generator
     *            {@link CobiGenWrapper} to be used for generation
     * @param monitor
     *            to track progress
     */
    public GenerateWizard(CobiGenWrapper generator, IProgressMonitor monitor) {

        super(generator, monitor);
        setWindowTitle(CobiGenDialogConstants.GenerateWizard.DIALOG_TITLE);
        initializeWizard();
    }

    /**
     * Initializes the wizard pages
     */
    private void initializeWizard() {
        page1 = new SelectFilesPage(cobigenWrapper, false);
        if (cobigenWrapper instanceof JavaInputGeneratorWrapper) {
            page2 = new SelectAttributesPage(
                ((JavaInputGeneratorWrapper) cobigenWrapper).getAttributesToTypeMapOfFirstInput());
        }
    }

    @Override
    public void addPages() {

        addPage(page1);
        if (cobigenWrapper instanceof JavaInputGeneratorWrapper) {
            addPage(page2);
        }
        //
        // if (cobigenWrapper instanceof JavaInputGeneratorWrapper) {
        // Job job = new Job("Collect Data for CobiGen Wizard Attribute Selection Page") {
        // @Override
        // protected IStatus run(IProgressMonitor monitor) {
        // Map<String, String> attributesToTypeMapOfFirstInput =
        // ((JavaInputGeneratorWrapper) cobigenWrapper).getAttributesToTypeMapOfFirstInput();
        // PlatformUIUtil.getWorkbench().getDisplay().asyncExec(() -> {
        // page2 = new SelectAttributesPage(attributesToTypeMapOfFirstInput);
        // addPage(page2);
        // });
        // return Status.OK_STATUS;
        // }
        // };
        // job.schedule();
        // setForcePreviousAndNextButtons(true);
        // }
    }

    // @Override
    // public boolean performFinish() {
    // buildPage2Job.cancel();
    // return super.performFinish();
    // }
    //
    // @Override
    // public boolean performCancel() {
    // buildPage2Job.cancel();
    // return super.performCancel();
    // }

    /**
     * Generates the contents to be generated and reports the progress to the user
     * @param dialog
     *            {@link ProgressMonitorDialog} which should be used for reporting the progress
     */
    @Override
    protected void generateContents(ProgressMonitorDialog dialog) {

        if (cobigenWrapper instanceof JavaInputGeneratorWrapper) {
            for (String attr : page2.getUncheckedAttributes()) {
                ((JavaInputGeneratorWrapper) cobigenWrapper).removeFieldFromModel(attr);
            }
        }

        LOG.info("Start generation process job...");
        GenerateSelectionJob job = new GenerateSelectionJob(cobigenWrapper, page1.getTemplatesToBeGenerated());
        try {
            dialog.run(true, true, job);
        } catch (InvocationTargetException e) {
            LOG.error("An internal error occured while invoking the generation job.", e);
        } catch (InterruptedException e) {
            LOG.warn("The working thread doing the generation job has been interrupted.", e);
        }
    }

}
