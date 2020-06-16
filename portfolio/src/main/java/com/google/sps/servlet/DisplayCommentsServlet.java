// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* Return a designed number of comments */
@WebServlet("/display-comments")
public class DisplayCommentsServlet extends HttpServlet{

    private int numComments;

    public void init(){
        numComments= 5;
    }

    /* Limits the maximum # of comments returned in single batch */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        Query query= new Query("Comment").addSort("submitTime", SortDirection.DESCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);

        List<Comment> comments= new ArrayList<>();
        for(Entity entity: results.asList(FetchOptions.Builder.withLimit(numComments))){
            long id = entity.getKey().getId();
            String userID= (String) entity.getProperty("fName");
            Date submitTime= (Date) entity.getProperty("submitTime");
            String comment= (String) entity.getProperty("comment");

            Comment newComment= new Comment(id, userID, submitTime, comment);
            comments.add(newComment);
        }

        Gson gson = new Gson();

        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(comments));
    }

    /* Changes query cap to user determined value */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
        numComments= Integer.parseInt(request.getParameter("numComments"));

        response.sendRedirect("/comments.html");
    }
}