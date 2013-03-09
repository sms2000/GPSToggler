#include <jni.h>
#include <android/log.h>

#include <sys/mount.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/stat.h>
#include <linux/stat.h>


#define LOG_TAG    			"SystemController"
#define OWNER_ROOT			0
#define MAX_BUFFER			4096

#define LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,   LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,    LOG_TAG, __VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,    LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,   LOG_TAG, __VA_ARGS__)


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// argv[0] - path to self
// argv[1] - command

// For command 'copy2system'
// argv[0] - source file path
// argv[1] - target file path
// argv[2] - device path
// argv[3] - mounting point
// argv[4] - file system type
// argv[5] - 'gid-uid' option (set GID/PID)

// For command 'remove4system'
// argv[0] - source file path
// argv[1] - device path
// argv[2] - mounting point
// argv[3] - file system type

// For command 'copy2data'
// argv[0] - source file path
// argv[1] - target file path
// argv[2] - 'gid-uid' option (set GID/PID)

// For command 'attributes'
// argv[0] - source file path
// argv[2] - 'gid-uid' option (set GID/PID)


int copy2system (int 	argc,
		  	  	 char	*argv[]);

int remove4system (int 	argc,
		  	  	   char	*argv[]);

int copy2data (int 	argc,
		  	   char	*argv[]);

int attributes (int 	argc,
		  	    char	*argv[]);

int main (int 		argc,
		  char		*argv[])
{
    LOGV("main. Entry...");

    if (setgid (0)
    	||
    	setuid (0))
    {
    	printf ("main. 'root' refused!\n");
        LOGE("main. 'root' refused!");
    }
    else
    {
    	printf ("main. 'root' acquired!\n");
        LOGW("main. 'root' acquired!");
    }

    LOGV("main. Number of parameters beyond the own path is %d",
    	 argc - 1);


    for (int i = 1; i < argc; i++)
    {
    	LOGV("        %s",
    		 argv[i]);
    }

    int ret = 0;

    if (argc < 2)
    {

    }
    else if (!strcmp (argv[1], "copy2system"))
    {
    	ret = copy2system (argc - 2,
    					   &argv[2]);
    }
    else if (!strcmp (argv[1], "remove4system"))
    {
    	ret = remove4system (argc - 2,
    					     &argv[2]);
    }
    else if (!strcmp (argv[1], "copy2data"))
    {
    	ret = copy2data (argc - 2,
    				   	 &argv[2]);
    }
    else if (!strcmp (argv[1], "attributes"))
    {
    	ret = attributes (argc - 2,
    				   	  &argv[2]);
    }

    LOGV("main. Exit with result: %d",
    	 ret);

    exit (0);
    return ret;
}


int copy2system (int 		argc,
		  	  	 char		*argv[])
{
	int	 retall 		= 0;
	bool option_giduid 	= false;

    LOGV("copy2system. Entry...");

// 0. Additional options
	if (argc > 5)
	{
		if (!strcmp (argv[5],
			"gid-uid"))
		{
			option_giduid = true;
			LOGD("copy2system. Raised option 'gid-uid'.");
		}
	}

// 1. Mount the system FS as r/w
    int ret = mount (argv[3],
    				 argv[2],
    				 argv[4],
    				 MS_MGC_VAL | MS_REMOUNT,
    				 "");

	if (ret)
	{
		retall = 1 << 0;
	}

	LOGD("copy2system. 'mount' for r/w returned %d",
	     ret);


// 2. Copy the stub file
    FILE *source = fopen (argv[0],
    					  "rb");
    if (NULL == source)
    {
   		retall = 1 << 1;
    	LOGE("copy2system. Source file fopen failed.");
    }
    else
    {
    	LOGV("copy2system. Source file fopen succeeded.");
    }

    FILE *target = fopen (argv[1],
    					  "wb");
    if (NULL == source)
    {
   		retall = 1 << 2;
    	LOGE("copy2system. Target file fopen failed.");
    }
    else
    {
    	LOGV("copy2system. Target file fopen succeeded.");
    }


	bool success = false;

	if (NULL != source
    	&&
    	NULL != target)
    {
    	char 	buffer[MAX_BUFFER];
    	int 	total 	= 0;
    	bool	copied 	= true;

    	for (;;)
    	{
    		int read = fread (buffer,
    						  1,
    						  MAX_BUFFER,
    						  source);

    		if (read < 1)
	    	{
	    		break;
	    	}

    		int written = fwrite (buffer,
    							  1,
    							  read,
    							  target);

    		if (written != read)
    		{
    			copied = false;
    			break;
    		}

    		total += read;
    	}

    	if (copied)
    	{
    		success = true;

    		LOGD("copy2system. Successfully copied %d bytes.",
	    		 total);
	    }
	    else
	    {
    		retall = 1 << 3;
	    	LOGE("copy2system. Copying failed.");
	    }
    }


    if (NULL != source)
    {
    	fclose (source);
    	LOGV("copy2system. Source stream closed.");
    }

    if (NULL != target)
    {
    	fclose (target);
    	LOGV("copy2system. Target stream closed.");
    }


// 3. Set permissions (is it really necessary?)
    if (success)
    {
    	int flags = S_IRUSR | S_IWUSR | S_IXUSR | S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH;
    	int ret;

    	if (option_giduid)
    	{
    		flags |= S_ISUID | S_ISGID;
    	}

    	ret = lchown (argv[1],
       			      OWNER_ROOT,
       			      OWNER_ROOT);

       	if (0 == ret)
        {
        	LOGD("copy2system. 'lchown' succeeded.");
        }
        else
        {
        	if (ret)
        	{
        		retall = 1 << 4;
        	}

        	LOGE("copy2system. 'lchown' failed.");
    	}


       	ret = chmod (argv[1],
    					 flags);

    	if (0 == ret)
	    {
	    	LOGD("copy2system. 'chmod' succeeded.");
	    }
	    else
	    {
	    	if (ret)
	    	{
	    		retall = 1 << 5;
	    	}

	    	LOGE("copy2system. 'chmod' failed.");
	    }
    }


// 4. Mount the system FS as r/o
    ret = mount (argv[3],
    			 argv[2],
    			 argv[4],
    			 MS_MGC_VAL | MS_REMOUNT | MS_RDONLY,
    			 "");

	if (ret)
	{
		retall = 1 << 5;
	}

	LOGD("copy2system. 'mount' for r/o returned %d",
         ret);

    LOGV("copy2system. Exit with code: %d",
    	 retall);

    return retall;
}


int remove4system (int 		argc,
		  	  	   char		*argv[])
{
    LOGV("remove4system. Entry...");

    int retall = 0;

// 1. Mount the system FS as r/w
    int ret = mount (argv[2],
    				 argv[1],
    				 argv[3],
    				 MS_MGC_VAL | MS_REMOUNT,
    				 "");
    if (ret)
    {
    	retall |= 1 << 0;
    }

    LOGD("remove4system. 'mount' for r/w returned %d",
         ret);


// 3. Remove the target
    ret = remove (argv[0]);
    if (ret)
    {
    	retall |= 1 << 1;
    }

    LOGD("remove4system. 'remove' for the target file returned %d",
         ret);


// 3. Mount the system FS as r/o
    sleep (1000);

    ret = mount (argv[2],
    			 argv[1],
    			 argv[3],
    			 MS_MGC_VAL | MS_REMOUNT | MS_RDONLY,
    			 "");
    if (ret)
    {
    	retall |= 1 << 2;
    }

    LOGD("remove4system. 'mount' for r/o returned %d",
         ret);


    LOGV("remove4system. Exit with code: %d",
    	 retall);

    return retall;
}


int copy2data (int 	argc,
		  	   char	*argv[])
{
	int	 retall 		= 0;
	bool option_giduid 	= false;

    LOGV("copy2data. Entry...");

// 0. Additional options
	if (argc > 2)
	{
		if (!strcmp (argv[2],
			"gid-uid"))
		{
			option_giduid = true;
			LOGD("copy2data. Raised option 'gid-uid'.");
		}
	}


// 1. Copy the stub file
    FILE *source = fopen (argv[0],
    					  "rb");
    if (NULL == source)
    {
   		retall = 1 << 0;
    	LOGE("copy2data. Source file fopen failed.");
    }
    else
    {
    	LOGV("copy2system. Source file fopen succeeded.");
    }

    FILE *target = fopen (argv[1],
    					  "wb");
    if (NULL == source)
    {
   		retall = 1 << 1;
    	LOGE("copy2data. Target file fopen failed.");
    }
    else
    {
    	LOGV("copy2data. Target file fopen succeeded.");
    }


	bool success = false;

	if (NULL != source
    	&&
    	NULL != target)
    {
    	char 	buffer[MAX_BUFFER];
    	int 	total 	= 0;
    	bool	copied 	= true;

    	for (;;)
    	{
    		int read = fread (buffer,
    						  1,
    						  MAX_BUFFER,
    						  source);

    		if (read < 1)
	    	{
	    		break;
	    	}

    		int written = fwrite (buffer,
    							  1,
    							  read,
    							  target);

    		if (written != read)
    		{
    			copied = false;
    			break;
    		}

    		total += read;
    	}

    	if (copied)
    	{
    		success = true;

    		LOGD("copy2data. Successfully copied %d bytes.",
	    		 total);
	    }
	    else
	    {
    		retall = 1 << 2;
	    	LOGE("copy2data. Copying failed.");
	    }
    }


    if (NULL != source)
    {
    	fclose (source);
    	LOGV("copy2data. Source stream closed.");
    }

    if (NULL != target)
    {
    	fclose (target);
    	LOGV("copy2data. Target stream closed.");
    }


// 2. Set permissions (is it really necessary?)
    if (success)
    {
    	int flags = S_IRUSR | S_IWUSR | S_IXUSR | S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH;

    	if (option_giduid)
    	{
    		flags |= S_ISUID | S_ISGID;
    	}

    	int ret = chmod (argv[1],
    					 flags);

    	if (0 == ret)
	    {
	    	LOGD("copy2data. 'chmod' succeeded.");
	    }
	    else
	    {
	    	if (ret)
	    	{
	    		retall = 1 << 3;
	    	}

	    	LOGE("copy2data. 'chmod' failed.");
	    }
    }


    LOGV("copy2data. Exit with code: %d",
    	 retall);

    return retall;
}


int attributes (int 	argc,
		  	    char	*argv[])
{
	int	 retall 		= 0;
	bool option_giduid 	= false;

    LOGV("attributes. Entry...");

// 0. Additional options
	if (argc > 1)
	{
		if (!strcmp (argv[1],
			"gid-uid"))
		{
			option_giduid = true;
			LOGD("attributes. Raised option 'gid-uid'.");
		}
	}


// 1. Set permissions (is it really necessary?)
   	int flags = S_IRUSR | S_IWUSR | S_IXUSR | S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH;
   	int ret;

   	if (option_giduid)
   	{
   		flags |= S_ISUID | S_ISGID;
   	}

   	ret = lchown (argv[0],
   			      OWNER_ROOT,
   			      OWNER_ROOT);

   	if (0 == ret)
    {
    	LOGD("attributes. 'lchown' succeeded.");
    }
    else
    {
    	if (ret)
    	{
    		retall = 1 << 0;
    	}

    	LOGE("attributes. 'lchown' failed.");
	}


   	ret = chmod (argv[0],
   				 flags);

   	if (0 == ret)
    {
    	LOGD("attributes. 'chmod' succeeded.");
    }
    else
    {
    	if (ret)
    	{
    		retall = 1 << 1;
    	}

    	LOGE("attributes. 'chmod' failed.");
	}


    LOGV("attributes. Exit with code: %d",
    	 retall);

    return retall;
}
