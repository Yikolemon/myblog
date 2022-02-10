package com.yikolemon.service;

import com.yikolemon.pojo.Blog;
import com.yikolemon.queue.ArchiveBlog;
import com.yikolemon.queue.IndexBlog;
import com.yikolemon.queue.RightTopBlog;
import com.yikolemon.queue.SearchBlog;

import java.util.List;
import java.util.Map;

public interface BlogService {

    Blog getBlog(Long id);

    Blog getAndConvert(Long id);

    int saveBlog(Blog blog);

    int updateBlog(Blog blog);

    int deleteBlog(Long id);

    List<Blog> listAllBlogs();

    List<IndexBlog> listBlogsIndex();

    List<IndexBlog> searchBlog(String title);

    List<RightTopBlog> listRecommendNewBlog(int size);

    List<RightTopBlog> listMostviewBlog(int size);

    List<Blog> listBlogsAdmin();

    List<Blog> listAllBlogsSearch(SearchBlog blog);

    List<IndexBlog> listBlogsByTypeId(Long id);

    List<IndexBlog> listBlogsByTagId(Long id);

    Map<String, List<ArchiveBlog>> listBlogsArchive();

    int countBlog();

    int updateView(Long id);

    int updateLike(Long id);

    Blog getLike(Long id);

}
