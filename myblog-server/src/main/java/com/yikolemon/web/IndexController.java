package com.yikolemon.web;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yikolemon.pojo.*;
import com.yikolemon.queue.IndexBlog;
import com.yikolemon.queue.IndexTag;
import com.yikolemon.queue.IndexType;
import com.yikolemon.queue.RightTopBlog;
import com.yikolemon.service.*;
import com.yikolemon.util.PageUtils;
import com.yikolemon.util.TopConfig;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author yikolemon
 */
@Controller
public class IndexController {

    @Resource
    private BlogService blogService;

    @Resource
    private TypeService typeService;

    @Resource
    private TagService tagService;

    @Resource
    private UserService userService;

    @Resource
    private LikeService likeService;

    int pageSize = PageUtils.getPageSize();

    int typeSize = TopConfig.getTypeSize();

    int tagSize = TopConfig.getTagSize();

    int blogSize = TopConfig.getBlogSize();

    @GetMapping("/")
    public String index(@RequestParam(defaultValue = "1") int pageNum, Model model) {
        PageHelper.startPage(pageNum, pageSize);
        List<IndexBlog> indexBlogs = blogService.listBlogsIndex();
        if (indexBlogs.size() == 0) {
            pageNum = 1;
            PageHelper.startPage(pageNum, pageSize);
            indexBlogs = blogService.listBlogsIndex();
        }
        PageInfo<IndexBlog> pageInfo = new PageInfo<>(indexBlogs);
        model.addAttribute("pageInfo", pageInfo);
        //获取总的记录数的方法  long total = pageInfo.getTotal();
        List<IndexType> typeTop = typeService.getTypeTop(typeSize);
        model.addAttribute("types", typeTop);
        List<IndexTag> tagTop = tagService.getTagTop(tagSize);
        model.addAttribute("tags", tagTop);
        List<RightTopBlog> recommendBlogs = blogService.listRecommendNewBlog(blogSize);
        model.addAttribute("recommendBlogs", recommendBlogs);
        List<RightTopBlog> viewBlogs = blogService.listMostviewBlog(blogSize);
        model.addAttribute("viewBlogs", viewBlogs);
        return "index";
    }


    @GetMapping("/blog/{id}")
    @Transactional
    public String blog(@PathVariable long id, Model model) {
        Blog blog = blogService.getAndConvert(id);
        blogService.updateViewOne(id);
        model.addAttribute("blog", blog);
        Long userId = blog.getUserId();
        User user = userService.getUser(userId);
        model.addAttribute("user", user);
        List<Tag> tags = tagService.getTagsByBlogId(blog.getId());
        model.addAttribute("tags", tags);
        Like like = likeService.getLike(id);
        model.addAttribute("like", like);
        return "blog";
    }

}
