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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.Entity;
import java.io.IOException;
import java.util.Date;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/new-comment")
public class NewCommentServlet extends HttpServlet{

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        response.setContentType("text/html");
        PrintWriter out= response.getWriter();

        UserService userService = UserServiceFactory.getUserService();
        if(userService.isUserLoggedIn()){
            out.println("<form action=\"/new-comment\" method=\"POST\">");
            out.println("<label for=\"fname\">First Name:</label>");
            out.println("<input type=\"text\" name=\"firstname\" placeholder=\"Your name..\">");
            out.println("<label for=\"lname\">Last Name:</label>");
            out.println("<input type=\"text\" name=\"lastname\" placeholder=\"Your last name..\">");
            out.println("<label for=\"gpoll\">Do You Work At Google:</label>");
            out.println("<label class=\"rb_container\">Yes");
            out.println("<input type=\"radio\" checked=\"checked\" name=\"radio\" value=\"yes\">");
            out.println("<span class=\"rb_checkmark\"></span>");          
            out.println("</label>");
            out.println("<label class=\"rb_container\">No");
            out.println("<input type=\"radio\" name=\"radio\" value=\"no\">");
            out.println("<span class=\"rb_checkmark\"></span>");
            out.println("</label>");
            out.println("<label class=\"rb_container\">Rather Not Say");
            out.println("<input type=\"radio\" name=\"radio\" value=\"rather not say\">");
            out.println("<span class=\"rb_checkmark\"></span>");
            out.println("</label>");
            out.println("<label for=\"about\">About You:</label>");
            out.println("<textarea id=\"subject\" name=\"subject\" placeholder=\"Tell me about you..\" style=\"height:200px\"></textarea>");
            out.println("<input type=\"submit\" value=\"Submit\">");
            out.println("</form>");
        }else {
            out.println("<p style=\"text-align:center\">Access Denied: Log In</p>");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse reponse) throws IOException{
        UserService userService = UserServiceFactory.getUserService();

        String fName= getParameter(request, "firstname", "Anonymous");
        String lName= getParameter(request, "lastname", "");
        String gPoll= checkGooglePoll(request);
        Date submitTime= new Date();
        String comment= getParameter(request, "subject", "");
        /* String userEmail= userService.getCurrentUser().getEmail(); */

        Entity commentEntity = new Entity("Comment");
        commentEntity.setProperty("fName", fName);
        commentEntity.setProperty("lName", lName);
        commentEntity.setProperty("gPoll", gPoll);
        commentEntity.setProperty("submitTime", submitTime);
        commentEntity.setProperty("comment", comment);
        /* commentEntity.setProperty("email", userEmail); */

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);

        reponse.sendRedirect("/comments.html");
    }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value= request.getParameter(name);
    if (value.isEmpty()) {
      return defaultValue;
    }
    return value;
  }

  private String checkGooglePoll(HttpServletRequest request){
      String value= request.getParameter("radio");
      
      if(value.equals("yes")){
        return "Yes";
      }else if(value.equals("no")){
        return "No";
      }  
      return "Rather Not Say";  
  }
}