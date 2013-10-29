## 20091123
## simus singesbeproc

GroupSize = 10;
TauInitiationBase = rep(1290*GroupSize, GroupSize);
TauRenonceAlpha = rep(0.009,GroupSize);
TauRenonceGamma = rep(2, GroupSize);
TauRenonceEpsilon = rep(2.3, GroupSize);
TauSuiviAlpha = rep(162.3, GroupSize);
TauSuiviBeta  = rep(75.4, GroupSize);

# BEE 2012.06.27
TauSuiviK = rep(1, GroupSize);
TauSuiviK[1]=0.025


TauInitiation = function(qui)
  {
    return ( TauInitiationBase[qui] );
  }
TauRenonce   = function(qui,partis)
  {
    return ( 1/ ( TauRenonceAlpha[qui]/(1+(partis/TauRenonceGamma[qui])^TauRenonceEpsilon[qui]) ) );
  }
TauSuivi = function(qui, partis,leader)
  {
    return (TauSuiviK[leader] * ( TauSuiviAlpha + TauSuiviBeta*(GroupSize-partis)/partis ));
  }

 
InitNb = 10000 ;
## moving size
MoveSizes = rep(0, GroupSize);
## nb of attempts per indiv
Attempts = rep(0,GroupSize);
## nb of success per indiv
Successs = rep(0,GroupSize);
##  moving orders
Ordres = matrix(0,GroupSize,GroupSize);


for (initrank in 1:InitNb)
  {
    #########    UNE SIMU\n");
    ordreDeparts = c();
    simul = data.frame(
      ident = 1:GroupSize,
      Restant = rep(1,GroupSize));
    simul$NextDates = rexp(GroupSize, 1/TauInitiation(simul$ident));
    initiateur = which.min(simul$NextDates);
    simul$Restant[initiateur]=0;
    ordreDeparts = c(ordreDeparts,initiateur);
    Attempts[initiateur] = Attempts[initiateur]+1;
    
    ## cat("\n\n\n") ; print(simul);
    while (1)
      {
        stillthere = length(which(simul$Restant==1));
        partis = GroupSize - stillthere;
        if (stillthere<1) break;

        simul$NextDates = rexp(GroupSize, 1/TauSuivi(simul$ident,partis,initiateur));
        simul$NextDates[simul$Restant<1]=NA;
        suiveur = which.min(simul$NextDates);

        dateRenonce = rexp(1,1/TauRenonce(initiateur,partis));

        if (dateRenonce < simul$NextDates[suiveur]) {
          ## cat("## give-up at ",dateRenonce,"  (< ",suiveur," = ",simul$NextDates[suiveur],")\n");
          break;
        } else {
          simul$Restant[suiveur]=0;
          ordreDeparts = c(ordreDeparts,suiveur);
        }
        ## cat("\n"); print(simul); print(ordreDeparts);
      }
    ###############################
    
    ## stocker le resultat
    ##     histo des tailles
    MoveSizes[partis] = MoveSizes[partis]+1;
    ## success
    if (partis == GroupSize)
          Successs[initiateur] = Successs[initiateur]+1;

    ##     ordres
    if (length(ordreDeparts)>1)
      {
        for (indiv in 1:(length(ordreDeparts)-1))
          {
            for (follow in (indiv+1):length(ordreDeparts))
              {
                Ordres[ordreDeparts[indiv],ordreDeparts[follow]] = Ordres[ordreDeparts[indiv],ordreDeparts[follow]]+1;
              }
          }
      }
  }
RepartSuccess = Successs/sum(Successs);
LeaderRepart = RepartSuccess[1]/(1/GroupSize);
AllResults = list(MoveSizes = MoveSizes,Attempts  = Attempts,Successs  = Successs, RepartSuccess = RepartSuccess, Orders    = Ordres);
#save(AllResults, file="all-equal.rda");
print(AllResults);
