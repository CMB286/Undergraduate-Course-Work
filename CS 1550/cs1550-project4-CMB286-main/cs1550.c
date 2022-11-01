#define FUSE_USE_VERSION 26

#include <errno.h>
#include <fcntl.h>
#include <fuse.h>
#include <stdio.h>
#include <string.h>

#include "cs1550.h"

/**
 * Called whenever the system wants to know the file attributes, including
 * simply whether the file exists or not.
 *
 * `man 2 stat` will show the fields of a `struct stat` structure.
 */
static int cs1550_getattr(const char *path, struct stat *statbuf)
{
	// Clear out `statbuf` first -- this function initializes it.
	memset(statbuf, 0, sizeof(struct stat));

	// Check if the path is the root directory.
	if (strcmp(path, "/") == 0) {
		statbuf->st_mode = S_IFDIR | 0755;
		statbuf->st_nlink = 2;

		return 0; // no error
	}

	char directory[MAX_FILENAME + 1];
	char fileName[MAX_FILENAME + 2];
	char extension[MAX_EXTENSION + 2];

	int result = sscanf(path, "/%[^/]/%[^.].%s", directory, fileName, extension);

	

	//Read root directory block
	FILE * fptr = fopen(".disk","rb+");
	struct cs1550_root_directory rb;
	fseek(fptr, 0, SEEK_SET);
	fread(&rb, sizeof(struct cs1550_root_directory), 1, fptr);

	// Check if the path is a subdirectory.
	if (result == 1) {
		for(int i = 0; i < rb.num_directories; i++){
			if(strncmp(directory, rb.directories[i].dname, MAX_FILENAME + 1) == 0){
				statbuf->st_mode = S_IFDIR | 0755;
	  			statbuf->st_nlink = 2;
				
				fclose(fptr);
	 			return 0; // no error
			}
		}
	}

	// Check if the path is a file.
	if ((result == 2 || result == 3)) {
		if(strlen(fileName) > MAX_FILENAME || strlen(extension) > MAX_EXTENSION){
			return -ENOENT;
		}

		struct cs1550_directory_entry directoryEntry;
		for(int i = 0; i < rb.num_directories; i++){
			if(strncmp(directory, rb.directories[i].dname, MAX_FILENAME + 1) == 0){
				fseek(fptr, rb.directories[i].n_start_block, SEEK_SET);
				fread(&directoryEntry, sizeof(struct cs1550_directory_entry), 1, fptr);
			}
		}

		for(int i = 0; i < directoryEntry.num_files; i++){
			if(strncmp(fileName, directoryEntry.files[i].fname, MAX_FILENAME + 1) == 0 || (strncmp(fileName, directoryEntry.files[i].fname,  MAX_FILENAME +  1) == 0 && strncmp(extension, directoryEntry.files[i].fext,  MAX_EXTENSION + 1) == 0)) {
				// Regular file
	 			statbuf->st_mode = S_IFREG | 0666;
	
	 			// Only one hard link to this file
	 			statbuf->st_nlink = 1;
	
	 			// File size -- replace this with the real size
	 			statbuf->st_size = directoryEntry.files[i].fsize;
				
				fclose(fptr);
				return 0; // no error
			}
		}	
	}

	fclose(fptr);
	// Otherwise, the path doesn't exist.
	return -ENOENT;
}

/**
 * Called whenever the contents of a directory are desired. Could be from `ls`,
 * or could even be when a user presses TAB to perform autocompletion.
 */
static int cs1550_readdir(const char *path, void *buf, fuse_fill_dir_t filler,
			  off_t offset, struct fuse_file_info *fi)
{
	(void) offset;
	(void) fi;

	char directory[MAX_FILENAME + 1];
	char fileName[MAX_FILENAME + 1];
	char extension[MAX_EXTENSION + 1];

	int result = sscanf(path, "/%[^/]/%[^.].%s", directory, fileName, extension);

	//Read root directory block
	FILE * fptr = fopen(".disk","rb+");
	struct cs1550_root_directory rb;
	fread(&rb, sizeof(struct cs1550_root_directory), 1, fptr);

	// This assumes no subdirectories exist. You'll need to change this.
	//if (strcmp(path, "/") != 0)
	//	return -ENOENT;

	// The filler function allows us to add entries to the listing.
	filler(buf, ".", NULL, 0);
	filler(buf, "..", NULL, 0);

	// Add the subdirectories or files.
	// The +1 hack skips the leading '/' on the filenames.
	//
	//for (each filename or subdirectory in path) {
	//	filler(buf, filename + 1, NULL, 0);
	//}


	//If at root, result will be 0 so can loop through list of directories
	if(strcmp(path, "/") == 0){
		for(int i = 0; i < rb.num_directories; i++){
			filler(buf, rb.directories[i].dname, NULL, 0);
		}
		
		fclose(fptr);
		return 0;
	}

	// If path is subdirectory, find the directory from the root
	if (result == 1) {
		char totalFileName[MAX_FILENAME + MAX_EXTENSION + 2];

		for(int i = 0; i < rb.num_directories; i++){
			if(strncmp(directory, rb.directories[i].dname, MAX_FILENAME + 1) == 0){
				size_t blockNumber;
				struct cs1550_directory_entry directoryEntry;

				blockNumber = rb.directories[i].n_start_block;
				fseek(fptr, blockNumber, SEEK_SET);
				fread(&directoryEntry, sizeof(struct cs1550_directory_entry), 1, fptr);

				for(int i = 0; i < directoryEntry.num_files; i++){
					strcpy(totalFileName, directoryEntry.files[i].fname);
					if(strlen(directoryEntry.files[i].fext) == MAX_EXTENSION){
						strcat(totalFileName, ".");
						strcat(totalFileName, directoryEntry.files[i].fext);
					}
					filler(buf, totalFileName, NULL, 0);
				}	
			}
		}

		fclose(fptr);
		return 0;
	}

	fclose(fptr);
	return -ENOENT;
}

/**
 * Creates a directory. Ignore `mode` since we're not dealing with permissions.
 */
static int cs1550_mkdir(const char *path, mode_t mode)
{
	(void) mode;

	//Read root directory block
	FILE * fptr = fopen(".disk","rb+");
	struct cs1550_root_directory rb;
	fseek(fptr, 0, SEEK_SET);
	fread(&rb, sizeof(struct cs1550_root_directory), 1, fptr);

	char directory[MAX_FILENAME + 2];
	char fileName[MAX_FILENAME + 1];
	char extension[MAX_EXTENSION + 1];

	int result = sscanf(path, "/%[^/]/%[^.].%s", directory, fileName, extension);

	//If result greater than 1, not at root
	if(result > 1) {
		fclose(fptr);
		return -EPERM;
	}

	//Need to check length of name
	if(strlen(directory) > MAX_FILENAME){
		fclose(fptr);
		return -ENAMETOOLONG;
	}

	//Need to check if directory exists
	for(int i = 0; i < rb.num_directories; i++) {
		if(strncmp(directory, rb.directories[i].dname, MAX_FILENAME + 1) == 0){
			fclose(fptr);
			return -EEXIST;
		}
	}

	//Return no space error if full
	if(rb.num_directories == MAX_DIRS_IN_ROOT) {
		fclose(fptr);
		return -ENOSPC;
	}

	//Create directory and initialize attributes
	struct cs1550_directory newDirectory;
	strcpy(newDirectory.dname, directory);
	newDirectory.n_start_block = rb.last_allocated_block + BLOCK_SIZE;

	//Place new directory in root and incremement number of directories and last allocated block
	rb.directories[rb.num_directories] = newDirectory;
	rb.num_directories++;
	rb.last_allocated_block += BLOCK_SIZE;

	//Place directory entry into disk at block
	struct cs1550_directory_entry initializeEntry;
	initializeEntry.num_files = 0;

	fseek(fptr, newDirectory.n_start_block, SEEK_SET);
	fwrite(&initializeEntry, sizeof(struct cs1550_directory_entry), 1, fptr);
	fflush(fptr);

	fseek(fptr, 0, SEEK_SET);
	fwrite(&rb, sizeof(struct cs1550_root_directory), 1, fptr);
	fflush(fptr);

	fclose(fptr);
	return 0;
}

/**
 * Removes a directory.
 */
static int cs1550_rmdir(const char *path)
{
	(void) path;
	return 0;
}

/**
 * Does the actual creation of a file. `mode` and `dev` can be ignored.
 */
static int cs1550_mknod(const char *path, mode_t mode, dev_t dev)
{
	(void) mode;
	(void) dev;


	char directory[MAX_FILENAME + 1];
	char fileName[MAX_FILENAME + 2];
	char extension[MAX_EXTENSION + 2];

	int result = sscanf(path, "/%[^/]/%[^.].%s", directory, fileName, extension);

	if(result <= 1){
		return -EPERM;
	}

	if(strlen(fileName) > MAX_FILENAME || strlen(extension) > MAX_EXTENSION){
		return -ENAMETOOLONG;
	}

	//Read root directory block
	FILE * fptr = fopen(".disk","rb+");
	struct cs1550_root_directory rb;
	fseek(fptr, 0, SEEK_SET);
	fread(&rb, sizeof(struct cs1550_root_directory), 1, fptr);

	size_t blockNumber = 0;
	struct cs1550_directory_entry directoryEntry;
	
	//Find directory, get block number, read directory block, add file to that entry
	if (result > 1) {
		for(int i = 0; i < rb.num_directories; i++){
			if(strncmp(directory, rb.directories[i].dname, MAX_FILENAME + 1) == 0){
				blockNumber = rb.directories[i].n_start_block;
				fseek(fptr, blockNumber, SEEK_SET);
				fread(&directoryEntry, sizeof(struct cs1550_directory_entry), 1, fptr);
			}
		}

		//Check if max number of files is hit
		if(directoryEntry.num_files == MAX_FILES_IN_DIR){
			fclose(fptr);
			return -ENOSPC;
		}

		for(int i = 0; i < directoryEntry.num_files; i++){
			if(strncmp(fileName, directoryEntry.files[i].fname, MAX_FILENAME + 1) == 0 && (strncmp(fileName, directoryEntry.files[i].fname,  MAX_FILENAME + 1) == 0 && strncmp(extension, directoryEntry.files[i].fext,  MAX_EXTENSION + 1) == 0)) {
				fclose(fptr);
				return -EEXIST;
			}
		}


		strcpy(directoryEntry.files[directoryEntry.num_files].fname, fileName);
		strcpy(directoryEntry.files[directoryEntry.num_files].fext, extension);
		directoryEntry.files[directoryEntry.num_files].fsize = 0;
		
		rb.last_allocated_block += BLOCK_SIZE;
		directoryEntry.files[directoryEntry.num_files].n_index_block = rb.last_allocated_block;

		struct cs1550_index_block indexBlock;

		fseek(fptr, rb.last_allocated_block, SEEK_SET);
		fread(&indexBlock, sizeof(struct cs1550_index_block), 1, fptr);

		//Point to data block
		rb.last_allocated_block += BLOCK_SIZE;
		indexBlock.entries[0] = rb.last_allocated_block;

		struct cs1550_data_block dataBlock;

		fseek(fptr, rb.last_allocated_block, SEEK_SET);
		fwrite(&dataBlock, sizeof(struct cs1550_data_block), 1, fptr);
		fflush(fptr);

		directoryEntry.num_files++;

		fseek(fptr, 0, SEEK_SET);
		fwrite(&rb, sizeof(struct cs1550_root_directory), 1, fptr);
		fflush(fptr);

		fseek(fptr, blockNumber, SEEK_SET);
		fwrite(&directoryEntry, sizeof(struct cs1550_directory_entry), 1, fptr);
		fflush(fptr);

		fseek(fptr, directoryEntry.files[directoryEntry.num_files - 1].n_index_block, SEEK_SET);
		fwrite(&indexBlock, sizeof(struct cs1550_index_block), 1, fptr);
		fflush(fptr);

		fclose(fptr);
		return 0;
	}

	fclose(fptr);
	return -EPERM;
}

/**
 * Deletes a file.
 */
static int cs1550_unlink(const char *path)
{
	(void) path;
	return 0;
}

/**
 * Read `size` bytes from file into `buf`, starting from `offset`.			NEED THIS
 */
static int cs1550_read(const char *path, char *buf, size_t size, off_t offset,
		       struct fuse_file_info *fi)
{
	(void) fi;
	(void) offset;
	char directory[MAX_FILENAME + 1];
	char fileName[MAX_FILENAME + 1];
	char extension[MAX_EXTENSION + 1];
	int result = sscanf(path, "/%[^/]/%[^.].%s", directory, fileName, extension);
	if(result <= 1){
		return -EISDIR;
	}
	//Read root block
	FILE * fptr = fopen(".disk","rb+");
	struct cs1550_root_directory rb;
	fseek(fptr, 0, SEEK_SET);
	fread(&rb, sizeof(struct cs1550_root_directory), 1, fptr);
	struct cs1550_directory_entry directoryEntry;
	for(int i = 0; i < rb.num_directories; i++){
		if(strncmp(directory, rb.directories[i].dname, MAX_FILENAME + 1) == 0){
			fseek(fptr, rb.directories[i].n_start_block, SEEK_SET);
			fread(&directoryEntry, sizeof(struct cs1550_directory_entry), 1, fptr);
		}
	}
	for(int i = 0; i < directoryEntry.num_files; i++){
		if(strncmp(fileName, directoryEntry.files[i].fname, MAX_FILENAME + 1) == 0 || (strncmp(fileName, directoryEntry.files[i].fname,  MAX_FILENAME +  1) == 0 && strncmp(extension, directoryEntry.files[i].fext,  MAX_EXTENSION + 1) == 0)) {
			//Here is where we find the file!
			struct cs1550_data_block dataBlock;
			struct cs1550_index_block indexBlock;
			size_t indexPosition = offset % BLOCK_SIZE;

			fseek(fptr, directoryEntry.files[i].n_index_block, SEEK_SET);
			fread(&indexBlock, sizeof(struct cs1550_index_block), 1, fptr);

			fseek(fptr, indexBlock.entries[indexPosition], SEEK_SET);
			fread(&dataBlock, sizeof(struct cs1550_data_block), 1, fptr);

			size_t readSize;
			if(size > BLOCK_SIZE){
				readSize = BLOCK_SIZE;
			}

			memcpy(buf, dataBlock.data, readSize);

	/*		if(size > MAX_DATA_IN_BLOCK){
				size -= MAX_DATA_IN_BLOCK;
				if(size > BLOCK_SIZE){
					readSize = BLOCK_SIZE;
				}
				else{
					readSize = size;
				}
				//Need to allocate another block
				fseek(fptr, indexBlock.entries[indexPosition + 1], SEEK_SET);
				fread(&dataBlock, sizeof(struct cs1550_data_block), 1, fptr);

				memcpy(buf, dataBlock.data, readSize);
			} */
			return size;
		}
	}

	return -ENOENT;
}

/**
 * Write `size` bytes from `buf` into file, starting from `offset`.	
 */ 
static int cs1550_write(const char *path, const char *buf, size_t size,
			off_t offset, struct fuse_file_info *fi)
{
	(void) fi;
	(void) offset;
	//Parse the path and get the root block
	char directory[MAX_FILENAME + 1];
	char fileName[MAX_FILENAME + 1];
	char extension[MAX_EXTENSION + 1];
	int result = sscanf(path, "/%[^/]/%[^.].%s", directory, fileName, extension);
	
	if(result <= 1){
		return -EISDIR;
	}
	//Read root block
	FILE * fptr = fopen(".disk","rb+");
	struct cs1550_root_directory rb;
	fseek(fptr, 0, SEEK_SET);
	fread(&rb, sizeof(struct cs1550_root_directory), 1, fptr);
	//Find directory and get block number
	struct cs1550_directory_entry directoryEntry;
	size_t blockNumber = 0;
	for(int i = 0; i < rb.num_directories; i++){
		if(strncmp(directory, rb.directories[i].dname, MAX_FILENAME + 1) == 0){
			blockNumber = rb.directories[i].n_start_block;
			fseek(fptr, rb.directories[i].n_start_block, SEEK_SET);
			fread(&directoryEntry, sizeof(struct cs1550_directory_entry), 1, fptr);
		}
	}
	for(int i = 0; i < directoryEntry.num_files; i++){
		if(strncmp(fileName, directoryEntry.files[i].fname, MAX_FILENAME + 1) == 0 || (strncmp(fileName, directoryEntry.files[i].fname,  MAX_FILENAME +  1) == 0 && strncmp(extension, directoryEntry.files[i].fext,  MAX_EXTENSION + 1) == 0)) {
			//Find specific file we're looking for
			struct cs1550_data_block dataBlock;

			struct cs1550_index_block indexBlock;

			size_t indexPosition = offset / BLOCK_SIZE;

			//Get index block number from file
			fseek(fptr, directoryEntry.files[i].n_index_block, SEEK_SET);
			fread(&indexBlock, sizeof(struct cs1550_index_block), 1, fptr);

			fseek(fptr, indexBlock.entries[indexPosition], SEEK_SET);
			fread(&dataBlock, sizeof(struct cs1550_data_block), 1, fptr);

			if(strlen(buf) > BLOCK_SIZE && size > BLOCK_SIZE){
				size = size - BLOCK_SIZE;
			}

			//Memcpy content of buf into data block in memory
			memcpy(dataBlock.data, buf, size);
			directoryEntry.files[i].fsize += strlen(buf);

			fseek(fptr, indexBlock.entries[indexPosition], SEEK_SET);
			fwrite(&dataBlock, sizeof(struct cs1550_data_block), 1, fptr);
			fflush(fptr);

			if(strlen(buf) > MAX_DATA_IN_BLOCK){
				//Need to allocate another block
				rb.last_allocated_block += BLOCK_SIZE;
				indexBlock.entries[indexPosition + 1] = rb.last_allocated_block;

				memcpy(dataBlock.data, buf + 512, size);

				fseek(fptr, indexBlock.entries[indexPosition + 1], SEEK_SET);
				fwrite(&dataBlock, sizeof(struct cs1550_data_block), 1, fptr);
				fflush(fptr);
			}

			//Write it back to disk
			fseek(fptr, blockNumber, SEEK_SET);
			fwrite(&directoryEntry, sizeof(struct cs1550_directory_entry), 1, fptr);
			fflush(fptr);

			fseek(fptr, directoryEntry.files[i].n_index_block, SEEK_SET);
			fwrite(&indexBlock, sizeof(struct cs1550_index_block), 1, fptr);
			fflush(fptr);

			fseek(fptr, 0, SEEK_SET);
			fwrite(&rb, sizeof(struct cs1550_root_directory), 1, fptr);
			fflush(fptr);

			return size;
		}

	}	
	return -ENOENT;
}


/**
 * Called when a new file is created (with a 0 size) or when an existing file
 * is made shorter. We're not handling deleting files or truncating existing
 * ones, so all we need to do here is to initialize the appropriate directory
 * entry.
 */
static int cs1550_truncate(const char *path, off_t size)
{
	(void) path;
	(void) size;
	return 0;
}

/**
 * Called when we open a file.
 */
static int cs1550_open(const char *path, struct fuse_file_info *fi)
{
	(void) fi;

	if (strcmp(path, "/") == 0) {
		return 0; //Found path
	}

	char directory[MAX_FILENAME + 1];
	char fileName[MAX_FILENAME + 1];
	char extension[MAX_EXTENSION + 1];

	int result = sscanf(path, "/%[^/]/%[^.].%s", directory, fileName, extension);

	//Read root directory block
	FILE * fptr = fopen(".disk","rb+");
	struct cs1550_root_directory rb;
	fread(&rb, sizeof(struct cs1550_root_directory), 1, fptr);

	// Check if the path is a subdirectory.
	if (result == 1) {
		for(int i = 0; i < rb.num_directories; i++){
			if(strncmp(directory, rb.directories[i].dname, MAX_FILENAME + 1) == 0){
				fclose(fptr);
	 			return 0; //Found path
			}
		}
	}

	// Check if the path is a file.
	if ((result == 2 || result == 3)) {
		size_t blockNumber;
		struct cs1550_directory_entry directoryEntry;

		for(int i = 0; i < rb.num_directories; i++){
			if(strncmp(directory, rb.directories[i].dname, MAX_FILENAME + 1) == 0){
				blockNumber = rb.directories[i].n_start_block;
				fseek(fptr, blockNumber, SEEK_SET);
				fread(&directoryEntry, sizeof(struct cs1550_directory_entry), 1, fptr);
			}
		}

		for(int i = 0; i < MAX_FILES_IN_DIR; i++){
			if(strncmp(fileName, directoryEntry.files[i].fname, MAX_FILENAME + 1) == 0 || (strncmp(fileName, directoryEntry.files[i].fname,  MAX_FILENAME + 1) == 0 && strncmp(extension, directoryEntry.files[i].fext,  MAX_EXTENSION + 1) == 0)) {
				fclose(fptr);
				return 0; // no error
			}
		}	
	}

	fclose(fptr);
    // If we can't find the desired file, return an error
    return -ENOENT;
}

/**
 * Called when close is called on a file descriptor, but because it might
 * have been dup'ed, this isn't a guarantee we won't ever need the file
 * again. For us, return success simply to avoid the unimplemented error
 * in the debug log.
 */
static int cs1550_flush(const char *path, struct fuse_file_info *fi)
{
	(void) path;
	(void) fi;

	// Success!
	return 0;
}

/**
 * This function should be used to open and/or initialize your `.disk` file.
 */
static void *cs1550_init(struct fuse_conn_info *fi)
{
	// Add your initialization routine here.
	(void) fi;

	return NULL;
}

/**
 * This function should be used to close the `.disk` file.
 */
static void cs1550_destroy(void *args)
{
	// Add your teardown routine here.
	(void) args;
}

/*
 * Register our new functions as the implementations of the syscalls.
 */
static struct fuse_operations cs1550_oper = {
	.getattr	= cs1550_getattr,
	.readdir	= cs1550_readdir,
	.mkdir		= cs1550_mkdir,
	.rmdir		= cs1550_rmdir,
	.read		= cs1550_read,
	.write		= cs1550_write,
	.mknod		= cs1550_mknod,
	.unlink		= cs1550_unlink,
	.truncate	= cs1550_truncate,
	.flush		= cs1550_flush,
	.open		= cs1550_open,
	.init		= cs1550_init,
	.destroy	= cs1550_destroy,
};

/*
 * Don't change this.
 */
int main(int argc, char *argv[])
{
	return fuse_main(argc, argv, &cs1550_oper, NULL);
}
