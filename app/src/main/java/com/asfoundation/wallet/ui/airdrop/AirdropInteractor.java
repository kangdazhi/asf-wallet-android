package com.asfoundation.wallet.ui.airdrop;

import com.asfoundation.wallet.Airdrop;
import com.asfoundation.wallet.AirdropData;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class AirdropInteractor {
  private final Airdrop airdrop;
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final AirdropChainIdMapper airdropChainIdMapper;
  private final EthereumNetworkRepositoryType repository;

  public AirdropInteractor(Airdrop airdrop, FindDefaultWalletInteract findDefaultWalletInteract,
      AirdropChainIdMapper airdropChainIdMapper, EthereumNetworkRepositoryType repository) {
    this.airdrop = airdrop;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.airdropChainIdMapper = airdropChainIdMapper;
    this.repository = repository;
  }

  public Single<String> requestCaptcha() {
    return findDefaultWalletInteract.find()
        .observeOn(Schedulers.io())
        .flatMap(wallet -> airdrop.requestCaptcha(wallet.address));
  }

  public Completable requestAirdrop(String captchaAnswer) {
    return findDefaultWalletInteract.find()
        .flatMap(wallet -> airdropChainIdMapper.getAirdropChainId()
            .doOnSuccess(chainId -> airdrop.request(wallet.address, chainId, captchaAnswer)))
        .toCompletable();
  }

  public Observable<AirdropData> getStatus() {
    return airdrop.getStatus()
        .doOnNext(airdropData -> {
          if (airdropData.getStatus()
              .equals(AirdropData.AirdropStatus.SUCCESS)) {
            repository.setDefaultNetworkInfo(airdropData.getNetworkId());
          }
        });
  }

  public void terminateStateConsumed() {
    airdrop.resetState();
  }
}
