package grisu.frontend.benchmark

import com.google.common.collect.Maps
import grisu.frontend.control.GJob
import grisu.jcommons.utils.PackageFileHelper
import grisu.jcommons.view.html.VelocityUtils

/**
 * Project: grisu
 *
 * Written by: Markus Binsteiner
 * Date: 31/05/13
 * Time: 12:31 PM
 */
class Utils {

    public static void createJobStub(File job_folder, String jobname) {

        if (job_folder.exists()) {
            println("Jobs folder already exists, not creating new one.")
            throw new RuntimeException("Job folder already exists: "+job_folder.getAbsolutePath())
        } else {
            job_folder.mkdirs()
            if (! job_folder.exists()) {
                throw new RuntimeException("Can't create directory "+job_folder.getAbsolutePath()+".")
            }
        }

        File temp = null
        temp = PackageFileHelper.getFile('job.config')
        new File(job_folder, GJob.JOB_PROPERTIES_FILE_NAME) << temp.text
        temp = new File(job_folder, GJob.FILES_DIR_NAME)
        temp.mkdirs()
        if ( ! temp.exists() ) {
            println ("Can't create folder: "+temp.getAbsolutePath())
            System.exit(1)
        }

        temp = PackageFileHelper.getFile('readme_files.txt')
        new File(job_folder, 'readme.txt') << temp.text

        File input_files = new File(job_folder, GJob.FILES_DIR_NAME)
        input_files.mkdirs()
        if ( ! input_files.exists() ) {
            println ("Can't create folder: "+input_files)
            System.exit(1)
        }
        temp = PackageFileHelper.getFile('example_input_file.txt')
        new File(input_files, 'example_input_file.txt') << temp.text

        Map properties = Maps.newHashMap()
        properties.put('job_dir', ".")
        String configContent = VelocityUtils.render('submit.config', properties)
        new File(job_folder, GJob.SUBMIT_PROPERTIES_FILE_NAME) << configContent
    }



}
