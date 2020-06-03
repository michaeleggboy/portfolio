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

import com.google.sps.data.Comment;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private List<Comment> comments;
  private int i;

  public void init(){
      comments= new ArrayList<>();
      i= 0;

      comments.add(new Comment(1, "Mario", new Date(), "Issa a-me, Mario!"));
      comments.add(new Comment(2, "Princess Peach", new Date(), "Thank you! Peace has at last returned to our fair Mushroom Kingdom."));
      comments.add(new Comment(3, "Luigi", new Date(), "Lets-a go!"));  
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {   

    if(i == comments.size())
        i = 0;        
    String json= convertToJsonUsingGson(comments.get(i));
    response.setContentType("application/json;");
    response.getWriter().println(json);
    i++;
  }

  /**
   * Converts a ServerStats instance into a JSON string using the Gson library. Note: We first added
   * the Gson library dependency to pom.xml.
   */
  private String convertToJsonUsingGson(Comment comment) {
    Gson gson = new Gson();
    String json = gson.toJson(comment);
    return json;
  }
}
