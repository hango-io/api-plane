package org.hango.cloud.mcp;

import org.hango.cloud.mcp.status.Status;
import org.hango.cloud.mcp.status.StatusMonitor;
import org.hango.cloud.mcp.status.StatusMonitorImpl;
import org.hango.cloud.mcp.status.StatusProductor;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @date 2020/6/11
 **/
public class StatusTest {
    private static final Logger logger = LoggerFactory.getLogger(StatusTest.class);

    @Test
    public void differenceTest() {
        Status s1 = new Status(new Status.Property[]{new Status.Property("231", "222"), new Status.Property("adv", "ggg")});
        Status s2 = new Status(new Status.Property[0]);
        Status.Difference diff = s1.compare(s2);
        Assert.assertThat(diff.getAdd().size(), equalTo(0));
        Assert.assertThat(diff.getUpdate().size(), equalTo(0));
        Assert.assertThat(diff.getDelete().size(), equalTo(2));
        Assert.assertThat(diff.getDelete(), allOf(hasItem(new Status.Property("231", "222")), hasItem(new Status.Property("adv", "ggg"))));

        diff = s2.compare(s1);
        Assert.assertThat(diff.getAdd().size(), equalTo(2));
        Assert.assertThat(diff.getAdd(), allOf(hasItem(new Status.Property("231", "222")), hasItem(new Status.Property("adv", "ggg"))));
        Assert.assertThat(diff.getUpdate().size(), equalTo(0));
        Assert.assertThat(diff.getDelete().size(), equalTo(0));


        s1 = new Status(new Status.Property[0]);
        diff = s1.compare(s2);
        Assert.assertThat(diff.getAdd().size(), equalTo(0));
        Assert.assertThat(diff.getUpdate().size(), equalTo(0));
        Assert.assertThat(diff.getDelete().size(), equalTo(0));

        s1 = new Status(new Status.Property[]{new Status.Property("111", "222"), new Status.Property("222", "333"), new Status.Property("333", "444")});
        s2 = new Status(new Status.Property[]{new Status.Property("444", "555"), new Status.Property("333", "345"), new Status.Property("222", "333")});
        diff = s1.compare(s2);
        Assert.assertThat(diff.getAdd(), hasItem(new Status.Property("444", "555")));
        Assert.assertThat(diff.getDelete(), hasItem(new Status.Property("111", "222")));
        Assert.assertThat(diff.getUpdate(), hasItem(new Status.Property("333", "345")));
        Assert.assertThat(diff.getAdd().size(), equalTo(1));
        Assert.assertThat(diff.getUpdate().size(), equalTo(1));
        Assert.assertThat(diff.getDelete().size(), equalTo(1));
    }

    @Test
    public void testMonitor() throws InterruptedException {
        AtomicInteger i = new AtomicInteger(0);
        // ???????????????????????????????????????????????????
        StatusMonitor monitor = new StatusMonitorImpl(1000L, new StatusProductor() {
            @Override
            public Status product() {
                i.incrementAndGet();
                throw new ApiPlaneException("test");
            }
        });
        monitor.start();
        Thread.sleep(5000L);
        Assert.assertThat(i.get() > 1, equalTo(true));
        monitor.shutdown();

        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        AtomicInteger i2 = new AtomicInteger(0);
        monitor = new StatusMonitorImpl(1000L, new StatusProductor() {
            @Override
            public Status product() {
                try {
                    i2.incrementAndGet();
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        monitor.start();
        Thread.sleep(5000L);
        Assert.assertThat(i2.get(), equalTo(2));
        monitor.shutdown();

        // Monitor?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        AtomicInteger i3 = new AtomicInteger(0);
        AtomicInteger i4 = new AtomicInteger(0);
        monitor = new StatusMonitorImpl(1000L, new StatusProductor() {
            @Override
            public Status product() {
                int index = i3.incrementAndGet();
                return new Status(new Status.Property[]{new Status.Property("index", "" + index)});
            }
        });
        // ??????????????????????????????
        monitor.registerHandler("index", (event, property) -> {
            logger.info(">> handler1, property index:{}", property.value);
            Assert.assertThat(i4.get(), anyOf(equalTo(3), equalTo(0)));
            i4.set(1);
        });
        monitor.registerHandler("index", (event, property) -> {
            logger.info(">> handler2, property index:{}", property.value);
            Assert.assertThat(i4.get(), equalTo(1));
            i4.set(2);
        });
        monitor.registerHandler("index", (event, property) -> {
            logger.info(">> handler3, property index:{}", property.value);
            Assert.assertThat(i4.get(), equalTo(2));
            i4.set(3);
        });

        monitor.start();
        Thread.sleep(7000L);
        monitor.shutdown();

        // ??????????????????????????????????????????????????????????????????????????????????????????????????????
        // ?????????????????????????????????
        AtomicInteger i5 = new AtomicInteger(0);
        monitor = new StatusMonitorImpl(1000L, new StatusProductor() {
            @Override
            public Status product() {
                int index = i5.incrementAndGet();
                return new Status(new Status.Property[]{new Status.Property("index", "" + index)});
            }
        });
        monitor.registerHandler("index", (event, property) -> {
            logger.info(">> handler1, property index:{}", property.value);
            logger.info(">> sleep 5s");
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        monitor.registerHandler("index", (event, property) -> {
            logger.info(">> handler2, property index:{}", property.value);
        });
        monitor.start();
        Thread.sleep(7000L);
        monitor.shutdown();
    }
}
