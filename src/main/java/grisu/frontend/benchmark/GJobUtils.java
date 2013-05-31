package grisu.frontend.benchmark;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import grisu.frontend.control.GJob;
import grisu.jcommons.utils.PackageFileHelper;
import grisu.jcommons.view.html.VelocityUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Project: grisu
 * <p/>
 * Written by: Markus Binsteiner
 * Date: 31/05/13
 * Time: 1:34 PM
 */
public class GJobUtils {

    public static void createJobStub(File job_folder, String jobname) throws IOException {

        if (job_folder.exists()) {
            System.out.println("Jobs folder already exists, not creating new one.");
            throw new RuntimeException("Job folder already exists: " + job_folder.getAbsolutePath());
        } else {
            job_folder.mkdirs();
            if (!job_folder.exists()) {
                throw new RuntimeException("Can't create directory " + job_folder.getAbsolutePath() + ".");
            }
        }

        File temp = null;
        temp = PackageFileHelper.getFile("job.config");
        Files.copy(temp, new File(job_folder, GJob.JOB_PROPERTIES_FILE_NAME));
        temp = new File(job_folder, GJob.FILES_DIR_NAME);
        temp.mkdirs();
        if (!temp.exists()) {
            System.out.println("Can't create folder: " + temp.getAbsolutePath());
            System.exit(1);
        }

        temp = PackageFileHelper.getFile("readme_files.txt");
        Files.copy(temp, new File(job_folder, "readme.txt"));

        File input_files = new File(job_folder, GJob.FILES_DIR_NAME);
        input_files.mkdirs();
        if (!input_files.exists()) {
            System.out.println("Can't create folder: " + input_files);
            System.exit(1);
        }
        temp = PackageFileHelper.getFile("example_input_file.txt");
        Files.copy(temp, new File(job_folder, "example_input_file.txt"));

        Map properties = Maps.newHashMap();
        properties.put("job_dir", ".");
        String configContent = VelocityUtils.render("submit.config", properties);
        Files.write(configContent, new File(job_folder, GJob.SUBMIT_PROPERTIES_FILE_NAME), Charsets.UTF_8);
    }
}
