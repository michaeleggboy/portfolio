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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/random-quote")
public class RandomQuoteServlet extends HttpServlet{

    private List<String> quotes;

    @Override
    public void init(){
        quotes = new ArrayList<>();

        quotes.add("Batman: (to Owlman) There is a difference between you and me. We both looked into the abyss, but when it looked back at us, you blinked.");
        quotes.add("Morpheus: There is a difference between knowing the path and walking the path.");
        quotes.add("Chewbacca: RRWWWGG.");
        quotes.add("Cobb: If you\'re going to perform inception, you need imagination.");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String quote = quotes.get((int) (Math.random() * quotes.size()));

        response.setContentType("text/html;");
        response.getWriter().println(quote);
    }

}