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

package com.google.sps;

import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.BitSet;

public final class FindMeetingQuery {  

  // Query ranges of time which meetings can be held based off already scheduled events and a meeting request //  
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) { 

    // if requested duration is more than a day then there are no options for meeting times
    if(request.getDuration() == TimeRange.WHOLE_DAY.duration() + 1){
        Collection<TimeRange> query = new ArrayList<>();
        return query;
    }

    ArrayList<Event> mandatorySchedule = getMandatorySchedule(events, request);
    ArrayList<Event> optionalSchedule = getOptionalSchedule(events, request);

    BitSet busy= getBusyTimes(mandatorySchedule);

    Collection<TimeRange> query = getQuery(busy, request.getDuration());
    return query;
  }

  private ArrayList<Event> getMandatorySchedule(Collection<Event> events, MeetingRequest request){
      ArrayList<Event> mandatorySchedule= new ArrayList<>();
      Collection<String> attendees = request.getAttendees();

      for(Event event: events){
          if(hasMandatoryAttendees(event, attendees)){
              mandatorySchedule.add(event);
          }
      }

      return mandatorySchedule;
  }

  private ArrayList<Event> getOptionalSchedule(Collection<Event> events, MeetingRequest request){
      ArrayList<Event> optionalSchedule= new ArrayList<>();
      Collection<String> optionalAttendees = request.getOptionalAttendees();

      for(Event event: events){
          if(hasOptionalAttendees(event, optionalAttendees)){
              optionalSchedule.add(event);
          }
      }

      return optionalSchedule;
  }

  private boolean hasMandatoryAttendees(Event event, Collection<String> attendees){
      return Collections.disjoint(event.getAttendees(), attendees) ? false : true;    
  }

  private boolean hasOptionalAttendees(Event event, Collection<String> optionalAttendees){
      return Collections.disjoint(event.getAttendees(), optionalAttendees) ? false : true;
  }

  private BitSet getBusyTimes(ArrayList<Event> mandatorySchedule){
      BitSet busy = new BitSet();

      for(Event event: mandatorySchedule){
          busy.set(event.getWhen().start(), event.getWhen().end() + 1);
      }

      return busy;
  }

  private ArrayList<TimeRange> getQuery(BitSet busy, long duration){
      ArrayList<TimeRange> query = new ArrayList<>();

      int start = busy.nextSetBit(0);
      int prev = busy.nextSetBit(0);

      for (int i = busy.nextSetBit(prev + 1); i >= 0; i = busy.nextSetBit(i + 1)) {
          if(prev != i - 1){
              int end = i;
              query.add(TimeRange.fromStartDuration(start, end - start));
              start = i;
          }

          prev = i;
      }

      return query;
  }
}