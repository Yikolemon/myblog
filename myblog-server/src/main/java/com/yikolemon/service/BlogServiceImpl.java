package com.yikolemon.service;

import com.github.pagehelper.PageHelper;
import com.yikolemon.mapper.BlogMapper;
import com.yikolemon.pojo.Blog;
import com.yikolemon.queue.ArchiveBlog;
import com.yikolemon.queue.IndexBlog;
import com.yikolemon.queue.RightTopBlog;
import com.yikolemon.queue.SearchBlog;
import com.yikolemon.service.search.BlogFullTextCompoment;
import com.yikolemon.util.MarkdownUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@CacheConfig(cacheNames = "blogs")
public class BlogServiceImpl implements BlogService{

    @Resource
    private BlogMapper blogMapper;
    @Resource
    private LikeService likeService;
    @Resource
    private BlogFullTextCompoment blogFullTextCompoment;

    private final ConcurrentHashMap<Long, Integer> viewMap = new ConcurrentHashMap<>();

    private final ScheduledExecutorService syncScheduler = Executors.newScheduledThreadPool(1);

    public BlogServiceImpl() {
        syncScheduler.scheduleAtFixedRate(this::syncViewDataToDatabase, 1, 2, TimeUnit.HOURS);
    }

    @Override
    @Cacheable(key = "'getBlog'+#id")
    public Blog getBlog(Long id) {
        return blogMapper.getBlog(id);
    }

    @Override
    @Cacheable(key = "'getAndConvert'+#id")
    public Blog getAndConvert(Long id) {
        PageHelper.clearPage();
        Blog blog = blogMapper.getBlog(id);
        String content = blog.getContent();
        String str = MarkdownUtils.markdownToHtmlExtensions(content);
        blog.setContent(str);
        return blog;
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void saveBlog(Blog blog) {
        blog.setCreateTime(new Date());
        blog.setUpdateTime(new Date());
        blog.setView(0);
        blogMapper.saveBlog(blog);
        blogFullTextCompoment.update(blog.getId());
    }

    @Override
    @CacheEvict(allEntries = true)
    public void updateBlog(Blog blog) {
        blog.setUpdateTime(new Date());
        blogMapper.updateBlog(blog);
        blogFullTextCompoment.update(blog.getId());
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public int deleteBlog(Long id) {
        likeService.deleteLike(id);
        blogFullTextCompoment.delete(id);
        return blogMapper.deleteBlog(id);
    }


    @Override
    //@Cacheable(key = "'listBlogsIndex'")
    public List<IndexBlog> listBlogsIndex() {
        return blogMapper.listBlogsIndex();
    }

    @Override
    public List<IndexBlog> searchBlog(String title) {
        return blogMapper.searchBlog(title);
    }

    @Override
    //@Cacheable(key = "'listRecommendNewBlog'+#size")
    public List<RightTopBlog> listRecommendNewBlog(int size) {
        return blogMapper.listRecommendNewBlog(size);
    }

    @Override
    //@Cacheable(key = "'listMostviewBlog'+#size")
    public List<RightTopBlog> listMostviewBlog(int size) {
        return blogMapper.listMostviewBlog(size);
    }

    @Override
    //@Cacheable(key = "'listBlogsAdmin'")
    public List<Blog> listBlogsAdmin() {
        return blogMapper.listBlogsAdmin();
    }

    @Override
    //@Cacheable(key ="listAllBlogsSearch+#blog")
    public List<Blog> listAllBlogsSearch(SearchBlog blog) {
        return blogMapper.listAllBlogsSearch(blog);
    }

    @Override
    @Cacheable(key = "'listBlogsByTypeId'+#id")
    public List<IndexBlog> listBlogsByTypeId(Long id) {
        return blogMapper.listBlogsByTypeId(id);
    }

    @Override
    @Cacheable(key = "'listBlogsByTagId'+#id")
    public List<IndexBlog> listBlogsByTagId(Long id) {
        return blogMapper.listBlogsByTagId(id);
    }

    @Override
    @Cacheable(key = "'listBlogsArchive'")
    public Map<String, List<ArchiveBlog>> listBlogsArchive() {
        String[] years = blogMapper.getAllYears();
        Map<String, List<ArchiveBlog>> map=new HashMap<>();
        for (String year : years) {
            List<ArchiveBlog> blogsByYear = blogMapper.getBlogsByYear(year);
            map.put(year, blogsByYear);
        }
        return map;
    }

    @Override
    @Cacheable(key = "'countBlog'")
    public int countBlog() {
        return blogMapper.countBlog();
    }

    @Override
    public void updateViewOne(Long id) {
        viewMap.compute(id, (key, value)->{
            if (value == null){
                Blog blog = blogMapper.getBlog(id);
                int view = blog.getView();
                return view + 1;
            }else{
                return value + 1;
            }
        });
    }

    // Synchronize view data from memory to the database
    public void syncViewDataToDatabase() {
        for (Map.Entry<Long, Integer> entry : viewMap.entrySet()) {
            Long blogId = entry.getKey();
            Integer views = entry.getValue();
            if (views != null && views > 0) {
                // Increment views in database
                blogMapper.updateView(blogId, views);
                // Reset local count after sync
                viewMap.remove(blogId);
            }
        }
        System.out.println("View data synchronized to database.");
    }

}
