package wepaht.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import wepaht.domain.PointHolder;
import wepaht.service.PointService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("export")
public class RestExportController {

    @Autowired
    PointService pointService;

    @RequestMapping(value = "/points",method = RequestMethod.GET)
    public List<PointHolder> getAllPoints() {
        List<PointHolder> points = new ArrayList<>();

        List<List<String>> rows = pointService.getAllPoints().getRows();
        for (List<String> row: rows) {
            PointHolder point = new PointHolder();
            point.setUsername(row.get(0));
            point.setPoints(Integer.valueOf(row.get(1)));

            points.add(point);
        }

        return points;
    }

    @RequestMapping(value = "/points/{username}",method = RequestMethod.GET)
    public PointHolder getPointsByUsername(@PathVariable String username) {
        PointHolder points = new PointHolder();
        points.setUsername(username);
        points.setPoints(pointService.getPointsByUsername(username));

        return points;
    }
}
