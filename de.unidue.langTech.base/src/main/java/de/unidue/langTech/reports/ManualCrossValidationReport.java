package de.unidue.langTech.reports;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;

public class ManualCrossValidationReport
    extends BatchReportBase
{

    public static IManualCrossValidation pointer;

    @Override
    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (subcontext.getType().contains("CRFSuite")) {

                String filePath = store
                        .getStorageFolder(subcontext.getId(),
                                "output/precisionRecallF1PerWordClass.txt").getParentFile()
                        .getParentFile().getAbsolutePath();

                int start = filePath.indexOf("CRFSuite");
                int end = filePath.indexOf("_", start);
                String name = filePath.substring(start, end);

                pointer.add(name, filePath);

            }
        }
    }

}
