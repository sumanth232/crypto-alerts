<%@ page import="java.util.Set" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.google.common.collect.Sets" %>
<%@ page import="com.example.helixdemo.MyClient" %>
<!DOCTYPE html>

<html lang="en">

<head>
    <link href="/resources/css/bootstrap.min.css" rel="stylesheet"/> </link>
    <%--<link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet">--%>
    <link href="/css/main.css" rel="stylesheet"/> </link>
</head>

<body>

<div id="page" class="container panel panel-default">
    <h3 class="text-center">Ideal State</h3>
    <div class="row">
        <%
            Map<String,Map<String,String>> instanceVsPartitionsIS = (Map<String,Map<String,String>>) request.getAttribute("instanceVsPartitionsIdealState");
            Set<String> servers = instanceVsPartitionsIS.keySet();
            Set<String> allServers = Sets.newHashSet("localhost:8085", "localhost:8086", "localhost:8087", "localhost:8088");
            Set<String> allPartitions = MyClient.getIdealStatePartitionSet();
            if (servers.size() > 0) {
                for (String server : allServers) {
                    String groupStyle = servers.contains(server) ? "" : "visibility:hidden";
        %>
        <span class="border">
                    <div class="list-group col-md-3" style="<%=groupStyle %>;">
                        <a href="#" class="list-group-item active"><%=server %></a>
        <%
            if ("visibility:hidden".equals(groupStyle)) {
                out.println("</div>");
                continue;
            }
            Map<String, String> partitionVsStateMap = instanceVsPartitionsIS.get(server);
            Set<String> partitionIds = partitionVsStateMap.keySet();
            System.out.println(partitionIds);
            System.out.println(allPartitions);
            for (String partition : allPartitions) {
                String groupItemStyle = partitionIds.contains(partition) ? "" : "visibility:hidden";

                String state = "NA";
                if (partitionIds.contains(partition)) {
                    state = partitionVsStateMap.get(partition);
                    state = "MASTER".equals(state) ? "M" : ("SLAVE".equals(state) ? "S" : "NA");
                }
        %>
                        <a href="#" class="list-group-item" style="<%=groupItemStyle %>;">
                            <%=partition %> <span class="badge"><%=state %></span>
                        </a>
                    <%}%>
                    </div>
                    </span>
        <%}
        }%>
    </div>
</div>

<div id="page" class="container panel panel-default">
    <h3 class="text-center">External View</h3>
    <div class="row">
        <%
            Map<String,Map<String,String>> instanceVsPartitionsEV = (Map<String,Map<String,String>>) request.getAttribute("instanceVsPartitionsExternalView");
            servers = instanceVsPartitionsEV.keySet();
            allServers = Sets.newHashSet("localhost:8085", "localhost:8086", "localhost:8087", "localhost:8088");
            allPartitions = MyClient.getIdealStatePartitionSet();
            if (servers.size() > 0) {
                for (String server : allServers) {
                    String groupStyle = servers.contains(server) ? "" : "visibility:hidden";
        %>
                    <span class="border">
                    <div class="list-group col-md-3" style="<%=groupStyle %>;">
                        <a href="#" class="list-group-item active"><%=server %></a>
        <%
                    if ("visibility:hidden".equals(groupStyle)) {
                        out.println("</div>");
                        continue;
                    }
                    Map<String, String> partitionVsStateMap = instanceVsPartitionsEV.get(server);
                    Set<String> partitionIds = partitionVsStateMap.keySet();
                    System.out.println(partitionIds);
                    System.out.println(allPartitions);
                    for (String partition : allPartitions) {
                        String groupItemStyle = partitionIds.contains(partition) ? "" : "visibility:hidden";

                        String state = "NA";
                        if (partitionIds.contains(partition)) {
                            state = partitionVsStateMap.get(partition);
                            state = "MASTER".equals(state) ? "M" : ("SLAVE".equals(state) ? "S" : "NA");
                        }
        %>
                        <a href="#" class="list-group-item" style="<%=groupItemStyle %>;">
                            <%=partition %> <span class="badge"><%=state %></span>
                        </a>
                    <%}%>
                    </div>
                    </span>
                <%}
            }%>
                <%--<div class="list-group col-md-3">--%>
                    <%--<a href="#" class="list-group-item active">Node-dummy</a>--%>
                    <%--<a href="#" class="list-group-item">--%>
                        <%--Partition-1 <span class="badge">M</span>--%>
                    <%--</a>--%>
                    <%--<a href="#" class="list-group-item disabled">--%>
                        <%--Partition-2 <span class="badge"></span>--%>
                    <%--</a>--%>
                    <%--<a href="#" class="list-group-item">--%>
                        <%--Partition-3 <span class="badge">S</span>--%>
                    <%--</a>--%>
                <%--</div>--%>
    </div>
</div>

<%--<script type="text/javascript" src="webjars/jquery/2.1.1/jquery.min.js"></script>--%>
<script type="text/javascript" src="/resources/js/bootstrap.min.js"></script>
</body>

</html>
