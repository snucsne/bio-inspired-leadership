
departed <- seq(1,10)
personality <- seq(0.1,0.9,0.1)
kfollow <- 2.828427*(1-personality)^1.5
kcancel <- 2.828427*(1-personality)^1.5
kfollow <- (10^(1/3.322))*(1-personality)^(3.322)
kcancel <- (10^(1/3.322))*(1-personality)^(3.322)

kfollow <- (4 * (1-personality)^2)
kcancel <- (4 * (1-personality)^2)

kfollow <- (2*(1-personality))
kcancel <- (2*(1-personality))

kfollow <- (1.4142*(1-personality)^(0.5))
kcancel <- (1.4142*(1-personality)^(0.5))


taufollow <- 162.3 + 75.4*(10-departed)/departed

taufollowp02 <- taufollow/kfollow[2]
taufollowp05 <- taufollow/kfollow[5]
taufollowp08 <- taufollow/kfollow[8]

followtimep02 <- rep(0,times=length(taufollowp02))
followtimep05 <- rep(0,times=length(taufollowp05))
followtimep08 <- rep(0,times=length(taufollowp08))


for(i in 1:length(taufollowp02)) {
	followtimep02[[i]] = mean(rexp(1000, 1/taufollowp02[[i]]))
}
for(i in 1:length(taufollowp05)) {
	followtimep05[[i]] = mean(rexp(1000, 1/taufollowp05[[i]]))
}
for(i in 1:length(taufollowp08)) {
	followtimep08[[i]] = mean(rexp(1000, 1/taufollowp08[[i]]))
}

followtimes <- data.frame( p02=followtimep02, p05=followtimep05, p08=followtimep08)

#png(filename="followtimes.png",height=600,width=800,bg="white")
postscript( file="followtimes.eps", height=3.5, width=4,
        onefile=FALSE, pointsize=10,
        horizontal=FALSE, paper="special" )
#par(mar=c(5.1,5.1,1.1,1.1))

plot(departed,followtimep02,type="o",col="red",ylim=c(0,2000),main="Mean follow time",xlab="Departed",ylab="Mean time")
lines(departed,followtimep05,type="o",col="blue",pch=22)
lines(departed,followtimep08,type="o",col="green",pch=23)
legend(7,2000,c("P=[0.2]","P=[0.5]","P=[0.8]"),col=c("red","blue","green"),lty=1,pch=21:23)

dev.off()




ccancel <- 0.009 / (1 + (departed/2)^2.3)

ccancelp02 <- ccancel*kcancel[2]
ccancelp05 <- ccancel*kcancel[5]
ccancelp08 <- ccancel*kcancel[8]

canceltimep02 <- rep(0,times=length(ccancelp02))
canceltimep05 <- rep(0,times=length(ccancelp05))
canceltimep08 <- rep(0,times=length(ccancelp08))


for(i in 1:length(ccancelp02)) {
	canceltimep02[[i]] = mean(rexp(1000, ccancelp02[[i]]))
}
for(i in 1:length(ccancelp05)) {
	canceltimep05[[i]] = mean(rexp(1000, ccancelp05[[i]]))
}
for(i in 1:length(ccancelp08)) {
	canceltimep08[[i]] = mean(rexp(1000, ccancelp08[[i]]))
}

canceltimes <- data.frame( p02=canceltimep02, p05=canceltimep05, p08=canceltimep08)

#png(filename="canceltimes.png",height=600,width=800,bg="white")
postscript( file="canceltimes.eps", height=3.5, width=4,
        onefile=FALSE, pointsize=10,
        horizontal=FALSE, paper="special" )

plot(departed,canceltimep02,type="o",col="red",ylim=c(0,2000),main="Mean cancel time",xlab="Departed",ylab="Mean time")
lines(departed,canceltimep05,type="o",col="blue",pch=22)
lines(departed,canceltimep08,type="o",col="green",pch=23)
legend(7,1250,c("P=[0.2]","P=[0.5]","P=[0.8]"),col=c("red","blue","green"),lty=1,pch=21:23)

dev.off()



postscript( file="combined-times.eps", height=3.5, width=4,
        onefile=FALSE, pointsize=10,
        horizontal=FALSE, paper="special" )

plot(departed,canceltimep02,type="o",col="red",ylim=c(0,2000),main="Mean cancel time",xlab="Departed",ylab="Mean time")
lines(departed,canceltimep05,type="o",col="blue",pch=22)
lines(departed,canceltimep08,type="o",col="green",pch=23)
lines(departed,followtimep02,type="o",col="red",pch=21)
lines(departed,followtimep05,type="o",col="blue",pch=22)
lines(departed,followtimep08,type="o",col="green",pch=23)
legend(7,1000,c("P=[0.2]","P=[0.5]","P=[0.8]"),col=c("red","blue","green"),lty=1,pch=21:23)

dev.off()


followtimes

canceltimes

